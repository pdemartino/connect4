package f4.stato;

/*
 * Stato.class
 * --------------------------------------------------------
 * LA classe rappresenta lo stato del gioco in un determinato istante.
 * Lo stato consiste nella configurazione della scacchiera.
 * ----
 * La scacchiera è una matrice di dimensioni TotYxTotX di celle (vedi classe Cella)
 * 
 *Metodi forniti:
 *
 * Stato:successore(int colonna, int pedina):
 * 		implementa la funzione successore; partendo dallo stato attuale e specificando l'azione (colonna + pedina), restituisce lo stato successivo
 * 		colonna deve essere compreso tra 1 e TotX
 * 		pedina deve essere 1 o -1
 * 
 * boolean:euristicaPresente()
 * 		implementa la funzione goal test restituendo true se lo stato attuale è considerato uno stato di goal, false altrimenti
 * 
 * int:euristicaFutura()
 * 		implementa la funzione euristica restituendo un valore numerico che rappresenta quanto lo stato attuale è vicino allo stato di goal
 */

import javax.swing.table.*;




public class Stato extends AbstractTableModel {
	
	
private int TOT_COLONNE = 7;
private int TOT_RIGHE = 8;


public int MAX_WIN=500;
public int MIN_WIN=-500;
public int NOT_DEF = -3000;
public int END_GAME = -200; 

//Ultima azione eseguita per arrivare allo stato attuale (necessaria per eseguire smartGoalTest)
private  int lastActionPerformend=-1;

public enum Direzione{ N,S,W,E,NW,NE,SW,SE} 

//In ogni istante uno stato è rappresentato dal contenuto della tavola di gioco e 
//dall'indicatore di turno
//IN Board Righe e Colonne sono invertite
//..... il primo indice indica la colonna, il secondo la riga
private int[][] board;
private int turno;
/*Valore della funzione euristica.
 * Valori restituiti:
 *    # -3000 variabile non inizializzata
 *    # -200 non esistono altre mosse
 *    # -500  stato di raggiungimento del goal per min
 *    # +500 stato di raggiungimento del goal per max
 */

/*private int MENO_INF = -500;
private int INF = 500;
private int NULL = -3000;
private int END_GAME*/ 
private int heuristic;


//Implementa la funzione successore variando la configurazione dello stato corrente
public void successore(int azione){
	
	if (azioneConsentita(azione)){//C'è ancora spazio nella colonna
		if (!this.booleanSmartGoalTest()){
			int pedina= turno;
			//Aggiorno array lastInserted (incremento top)
			this.board[azione -1][TOT_RIGHE]++;
			//Aggiungo pedina
			this.board[azione -1][getTop(azione-1)]= pedina;
			switchTurno();	
			this.lastActionPerformend=azione;
		}
	}
	
}

public Stato getSuccessore(int azione){
	
	if (azioneConsentita(azione) && (!this.booleanSmartGoalTest())){//C'� ancora spazio nella colonna
		int pedina= this.turno;
		Stato succ= new Stato();
					
		//Istanzio matrice
		succ.board=new int[TOT_COLONNE][TOT_RIGHE+1];
		//Effettuo la copia del vecchio stato
		for(int i=0;i<TOT_COLONNE;i++){
			for(int j=0;j<TOT_RIGHE+1;j++){
				succ.board[i][j]= this.board[i][j];
			}
		}
		
		//	Aggiorno array lastInserted
		succ.board[azione -1][TOT_RIGHE]++;
		//	Aggiungo pedina
		succ.board[azione -1][succ.getTop(azione -1)]= pedina;
		//Cambio turno
		succ.turno=this.turno*(-1);
		succ.lastActionPerformend=azione;
		return succ;
	}else
		return this;
}


public boolean booleanSmartGoalTest(){
	return (Math.abs(this.smartGoalTest())==500);
}

//Implementa un goaltest a bassa complessit� analizzando prima i cambiamenti causati dall'ultima azione
//Nel caso in cui l'ultima azione non abbia portato al goal invoca il goaltest standard
public int smartGoalTest(){ 
	
	//int colonna= lastAction-1;
	if (this.lastActionPerformend==-1)
		return 0;
	
	int colonna = this.lastActionPerformend -1;
	int riga = this.board[colonna][TOT_RIGHE];
	
	int check = this.board[colonna][riga];
	int adiacenti=0;
	////////////// Check VERTICALE ////////
	adiacenti= cercaAdiacenze(Direzione.S, riga, colonna);
	if (adiacenti==3){
		if(check==1)
			return MAX_WIN;
		else 
			return MIN_WIN;
	}
	
	///////// CHECK ORIZZONTALE ////////
	adiacenti= (cercaAdiacenze(Direzione.E, riga, colonna) + cercaAdiacenze(Direzione.W, riga, colonna));
        //System.out.println("Adiacenti: "+ adiacenti);
	if (adiacenti>=3){
		if(check==1)
			return MAX_WIN;
		else 
			return MIN_WIN;
	}
	
	////Check DIAGONALE
	adiacenti = (cercaAdiacenze(Direzione.NE, riga, colonna) + cercaAdiacenze(Direzione.SW, riga, colonna));
	if (adiacenti>=3){
		if(check==1)
			return MAX_WIN;
		else 
			return MIN_WIN;
	}
	
	adiacenti = (cercaAdiacenze(Direzione.NW, riga, colonna) + cercaAdiacenze(Direzione.SE, riga, colonna));
	if (adiacenti>=3){
		if(check==1)
			return MAX_WIN;
		else 
			return MIN_WIN;
	}
	
	return 0;
	
}

//Effettua una valutazione dello stato attuale implementando, di conseguenza,
//anche la funzione di euristicaPresente
//-----------------------------------------------------
//Implementazione
//Per ogni top di colonna esamino gli elementi adiacenti contando:
//- il numero di coppie
//- il numero di terne
//- il numero di quaterne <- GOAL
//coppie e terne vengono considerate valide solo se utili a concorrere per un forza 4 (ancora celle vuote a disp)

public int euristicaPresente(){
	
	//Map<Giocatore,Map<[terna|coppia],numero_Nple>>
	//...Nple[0]= coppie, ..Nple[1]=terne
	int maxNple[]= {0,0};
	int minNple[]= {0,0};
	
	int adiacenti;
	int euristicaMax=0;
	int euristicaMin=0;
	/////////////////////////////////////////////////// CHECK VERTICALE ///////////////////////////////////////////////////////
	//Per ogni colonna, 
	for (int x=0; x<TOT_COLONNE;x++){
		//Partendo dall'ultima pedina inserita nella colonna
		int top= this.board[x][TOT_RIGHE];//Indice di riga ultima pedina inserita
		if(top>-1){//E' stata inserita almeno una pedina
			int check= this.board[x][top];//Pedina
		
			adiacenti=cercaAdiacenze(Direzione.S, top, x);//Cerco adiacenze a sud
			
			if (adiacenti==3){
				//GOAAAAL
				if(check==1)
					return MAX_WIN;
				else
					return MIN_WIN;
				
		 	}else if (adiacenti>0){
				//Controllo se a nord ci sono abbastanza caselle disponibili per completare il forza 4
				////System.out.print("Colonna " + x + ": " + adiacenti + " adiacenze trovate per la pedina " + check);
		 		double intorno = cercaVuoti(check,Direzione.N,top, x,adiacenti,-1);
		 		
				if(check==1)
					euristicaMax +=intorno;
				else
					euristicaMin +=intorno;
				
				////System.out.println("check:  "+check+ "valore vuoti : "+ intorno);
			}
			
		}
	}
	
	
	///////////////////////////////////////////////// CHECK ORIZZONTALE ///////////////////////////////////////////////////////////////
	
	//Definisco il range in cui cercare
	//Inizia dalla riga= minimo dei top, si8 ferma sul massimo dei top
	
	//Calcolo max(top) e min(top) //Mi serviranno anche dopo per controllo diagonale
	int MIN_TOP = TOT_RIGHE;
	int MAX_TOP=0;
	for(int i=0; i<TOT_COLONNE;i++){
		if (this.board[i][TOT_RIGHE]<MIN_TOP){
			MIN_TOP=this.board[i][TOT_RIGHE];
		}
		if (this.board[i][TOT_RIGHE]>MAX_TOP){
			MAX_TOP=this.board[i][TOT_RIGHE];
		}
	}
	if (MIN_TOP==-1) MIN_TOP=0;

	//Effettuo la ricerca nel range
	for(int riga=MIN_TOP; riga<=MAX_TOP; riga++){
		int colonna=0; //L'indice di colonna deve essere opportunamente gestito in modo da rilevare + di una npla sulla stessa riga
		int lowerBound=-1; // limite inferiore per il controllo dei vuoti
		while (colonna <= (TOT_COLONNE-2)){//Se non esistono almeno un'altra casella a destra � inutile cercare (non ci pu� essere nemmeno la coppia 
			
			int check= this.board[colonna][riga];//Check � la pedina in quella cella
			if (check!=0){//Se non si tratta di una cella vuota
				adiacenti= cercaAdiacenze(Direzione.E, riga, colonna);
				
				if (adiacenti>=3){ //3 adiacenze + pedina corrente= 4=> FORZA 4! VITTORIA!
					if(check==1)
						return MAX_WIN;
					else
						return MIN_WIN;
					
				}else if(adiacenti>0){
					
					colonna= colonna+adiacenti;
					
					//	Controllo se a DX e/o a SX ci sono abbastanza caselle disponibili per completare il forza 4
					double vuotidx= cercaVuoti(check,Direzione.E,riga,colonna,adiacenti,lowerBound); 
					double vuotisx= cercaVuoti(check,Direzione.W, riga, colonna-adiacenti,adiacenti,lowerBound);
					
					lowerBound=colonna;//Viene settato il lower bound per l'iterata successiva
					double vuoti;
					if ((vuotisx==1.5)&&(vuotidx==1.5))
						vuoti = vuotisx + vuotidx;
					else 
						vuoti =Math.floor( Math.max(vuotisx, vuotidx));
					
					if (check ==1)
						euristicaMax += (new Double(vuoti)).intValue();
					else
						euristicaMin += (new Double(vuoti)).intValue();
					////System.out.println("check:  "+check+ "valore vuoti : "+ vuoti);
					
					
					//colonna=colonna+vuoti;
					
				}else{//adiacenti===0
					colonna++;
				}
					
			}else//La cella � vuota (check==0)
				colonna++;//Passo a controllare la prossima cella sulla stessa riga
		}
	}
		
	///////////////////////// CHECK DIAGONALE //////////////////////////////////////
	// Il controllo in diagonale si divide in II fasi
	// I fase: controllo diagonale NE-SW
	// II fase: controllo della diagonale  NW-SE
	// Il controllo viene effettuato nella sottomatrice max(top) x 7 (+ controllo celle vuote superiori) esaminando ogni elemento della sottomatrice.
	// Al fine di non contare pi� volte la stessa diagonale presa da diversi punti di vista, il controllo tiene traccia delle celle gi� visitate mediante una matrice max(top) x 7 ausiliaria
	
	
	boolean[][] matriceAusiliaria= new boolean[MAX_TOP+1][TOT_COLONNE];
	//InitMAtrice
	for (int i=0;i< MAX_TOP+1;i++)
		for(int j=0;j<TOT_COLONNE;j++)
			matriceAusiliaria[i][j]=false;
		
		
	
	//I Fase (Diagonali NE-SW) (da effettuare solo su celle con colonna <= (TOT_COLONNE-4)
	////System.out.println("Controllo diagonale NE-SW");
	for(int x=0; x< TOT_COLONNE; x++){
		int lowerBound = -1;
		
		for (int y=0; y<=MAX_TOP;y++){
			//	Controllo se l'elemento fa gi� parte di un forza 4 analizzato in precedenza
			if ((!matriceAusiliaria[y][x]) && (this.board[x][y]!=0)){//Ancora non ho considerato la cella e la cella non � vuota...procedo al controllo
				int check =this.board[x][y];
				int adiacenzeNE= cercaAdiacenze(Direzione.NE, y,x);
				int adiacenzeSW= cercaAdiacenze(Direzione.SW, y, x);
				int totAdiacenze= adiacenzeNE+ adiacenzeSW;
				
				////System.out.println("Pedina " + check + " di coordinate [" + x + "," + y + "]: adiacenzeNE: " + adiacenzeNE + " adiacenzeSW:" + adiacenzeSW);
				if(totAdiacenze>=3){//3 adiacenze + 1 pedina corrente= 4 => FORZA 4!
					if (check==1)
						return MAX_WIN;
					else 
						return MIN_WIN;
				}else if(totAdiacenze>0){//ho almeno una pedina adiacente=> posso fare almeno una coppia
					//Marco le adiacenti come controllate nella matriceAusiliaria
					for(int i=1; i<=adiacenzeNE; i++)
						matriceAusiliaria[y+i][x+i]=true;
					for(int i=1; i<=adiacenzeSW; i++)
						matriceAusiliaria[y-i][x-i]=true;
					int h = y-adiacenzeSW;
					int k= x-adiacenzeSW; 
					while ((h>=0)&& (k>=0)){
						if (matriceAusiliaria[h][k]==false){
							h--;
							k--;
						}
						else break;
					}
					lowerBound = k;
					double vuotiSx = cercaVuoti(check,Direzione.SW, y-adiacenzeSW, x-adiacenzeSW,totAdiacenze,lowerBound);  
					double vuotiDx = cercaVuoti(check,Direzione.NE, y+adiacenzeNE, x+adiacenzeNE,totAdiacenze,-1);
					//controllo se ci sono abbastanza caselle vuote per completare il forza4 (verso NE x e y aumentano, verso SW x e y diminuiscono
					
					double vuoti;
					if ((vuotiSx==1.5)&&(vuotiDx==1.5))
						vuoti = vuotiSx + vuotiDx;
					else 
						vuoti =Math.floor( Math.max(vuotiSx, vuotiDx));
					
					if (check ==1)
						euristicaMax += (new Double(vuoti)).intValue();
					else
						euristicaMin += (new Double(vuoti)).intValue();
					////System.out.println("check:  "+check+ "valore vuoti : "+ vuoti);
				}
				
			}
		}
	}	
	
	//Reset Matrice ausiliaria
	for (int i=0;i< MAX_TOP+1;i++)
		for(int j=0;j<TOT_COLONNE;j++)
			matriceAusiliaria[i][j]=false;
		
	// II Fase(Diagonali NW-SE= (da effettuare solo su celle con colonna >=3
	////System.out.println("Diagonale NW-SE");
	for(int x=0; x< TOT_COLONNE; x++){
		for (int y=0; y<=MAX_TOP;y++){
			//	Controllo se l'elemento fa gi� parte di un forza 4 analizzato in precedenza
			if ((!matriceAusiliaria[y][x]) && (this.board[x][y]!=0)){//Ancora non ho considerato la cella e la cella non � vuota...procedo al controllo
				int check =this.board[x][y];
				int adiacenzeNW= cercaAdiacenze(Direzione.NW, y,x);
				int adiacenzeSE= cercaAdiacenze(Direzione.SE, y, x);
				int totAdiacenze= adiacenzeNW+ adiacenzeSE;
				
				////System.out.println("Pedina " + check + " di coordinate [" + x + "," + y + "]: adiacenzeNW: " + adiacenzeNW + " adiacenzeSE:" + adiacenzeSE);
				
				if(totAdiacenze>=3){//3 adiacenze + 1 pedina corrente= 4 => FORZA 4!
					if (check==1)
						return MAX_WIN;
					else 
						return MIN_WIN;
				}else if(totAdiacenze>0){//ho almeno una pedina adiacente=> posso fare almeno una coppia
					//Marco le adiacenti come controllate nella matriceAusiliaria
					for(int i=1; i<=adiacenzeNW; i++)
						matriceAusiliaria[y+i][x-i]=true;
					for(int i=1; i<=adiacenzeSE; i++)
						matriceAusiliaria[y-i][x+i]=true;
					
					int h = y+adiacenzeNW;
					int k= x-adiacenzeNW; 
					while ((h<=MAX_TOP)&& (k>=0)){
						if (matriceAusiliaria[h][k]==false){
							h++;
							k--;
						}
						else break;
					}
					int lowerBound = k;
					
					double vuotiDx = cercaVuoti(check,Direzione.SE, y-adiacenzeSE, x+adiacenzeSE, totAdiacenze, TOT_COLONNE);
					double vuotiSx = cercaVuoti(check,Direzione.NW, y+adiacenzeNW, x-adiacenzeNW, totAdiacenze, lowerBound); 
					//controllo se ci sono abbastanza caselle vuote per completare il forza4 (verso NW x dim e y aumenta, verso SE x aum e y dim
					
					double vuoti;
					if ((vuotiSx==1.5)&&(vuotiDx==1.5))
						vuoti = vuotiSx + vuotiDx;
						
					else 
						vuoti =Math.floor( Math.max(vuotiSx, vuotiDx));
					
					if (check ==1)
						euristicaMax += (new Double(vuoti)).intValue();
					else
						euristicaMin += (new Double(vuoti)).intValue();
					////System.out.println("check:  "+check+ " vuotiDx: " + vuotiDx + " vuotiSx: " + vuotiSx + " valore vuoti : "+ vuoti);
					
				}
				
			}
		}
	}
		
	
	//FINE CONTROLLI
	////System.out.println("Max : "+ euristicaMax+ " MIN:  "+ euristicaMin);
	return euristicaMax - euristicaMin;
}





//Cerca pedine dello stesso segno adiacenti
//Spostandosi nella direzione indicata
//direzione:
//	1: Nord, 2:Sud, 3:Est, 4:Ovest, 5: NE, 6: SE, 7:NW, 8:SE
private int cercaAdiacenze(Direzione direzione, int riga, int colonna){
	
	int check= this.board[colonna][riga];
	int adiacenti=0;
	
	switch (direzione){
	case N://Nord (non implementato perche non usato mai
		break;
		
	case S: //Sud //Usato per il controllo in verticale
		//mi sposto massimo di 4 caselle verso sud(di meno se l'altezza � inferiore), l'altezza � data dall'indice di riga
		
		for(int delta=1; delta<= Math.min(3, riga); delta++ ){
			if (this.board[colonna][(riga-delta)]==check)
				adiacenti ++;
			else
				break;
		}
		break;
		
	case E: //Est
		
		for(int delta=1; delta<= Math.min(3,(TOT_COLONNE -1) -colonna);delta++){
			if(this.board[colonna+delta][riga]==check)
				adiacenti ++;
			else
				break;
		}
		break;
	
	
	case W://Ovest

                for(int delta=1; delta<= Math.min(3,colonna);delta++){
			if(this.board[colonna-delta][riga]==check)
				adiacenti ++;
			else
				break;
		}
		break;
	case NE://x/colonna: aumenta y/riga: aumenta
		for(int delta=1; delta <= Math.min(3,Math.min((TOT_RIGHE-riga-1),(TOT_COLONNE-1)-colonna)); delta++){
			if(this.board[colonna+delta][riga+delta]==check)
				adiacenti++;
			else
				break;
		}
		break;
		
	case NW://x/colonna: diminuisce, y/riga: aumenta
		for(int delta=1; delta <= Math.min(3,Math.min((TOT_RIGHE-riga-1),colonna)); delta++){
			if(this.board[colonna-delta][riga+delta]==check)
				adiacenti++;
			else
				break;
		}
		break;
		
	case SE://x/colonna: aumenta, y/riga: diminuisce
		for(int delta=1; delta <= Math.min(3, Math.min(riga, TOT_COLONNE-colonna-1)  ); delta++){
			if(this.board[colonna+delta][riga-delta]==check)
				adiacenti++;
			else
				break;
		}
		break;
		
	case SW://x/colonna: diminuisce, y/riga: diminuisce
		for(int delta=1; delta <= Math.min(3, Math.min(riga,colonna)); delta++){
			if(this.board[colonna-delta][riga-delta]==check)
				adiacenti++;
			else
				break;
		}
		break;
	}
	return adiacenti;
}


//Cerca caselle ancora vuote a partire dalla cella successiva a quella indicata nella direzione indicata
//direzione:
//	1: Nord, 2:Sud, 3:Est, 4:Ovest, 5: NE, 6: SE, 7:NW, 8:SE

private double cercaVuoti(int giocatore, Direzione direzione, int riga, int colonna,int adiacenze,int lowerBound){
	
	int restanti = 4-(adiacenze+1);
	
	/*
	 * Una casella vuota immediatamente conquistabile = agibile
	 * Una casella vuota non immediatamente conquistabile = non agibile
	 * 
	 * Una casella contenente pedina dello stesso seme = pedina.
	 */
	
	int AGIBILE_AGIBILE = 3;
	int NON_AGIBILE_NON_AGIBILE = 1;
	int AGIBILE_PEDINA = 4;
	int NON_AGIBILE_PEDINA = 2;
	double AGIBLE_NON_AGIBILE = 1.5;
	
	/*
	 * Interi contatori di caselle rilevate 
	 */
	
	int agibile = 0;
	int nonAgibile = 0;
	
	int pedina=0;
	if (restanti==1)
		pedina=1;
	
	switch (direzione){
	case N://Nord => non � necessario eseguire un ciclo
		if (((this.TOT_RIGHE-1) - this.getTop(colonna)) >= restanti )
			return AGIBILE_AGIBILE;
		break;
	
	case E: //Est
			
		for(int delta=1; delta<= Math.min(restanti,(TOT_COLONNE-colonna-1));delta++){
			
			if(this.board[colonna+delta][riga]==0){//casella vuota
				//Verifico se la casella sottostante � piena
				if (riga>0)
					if(this.board[colonna+delta][riga-1]!=0)
						agibile ++;
					else
						nonAgibile++;
				else//La casella vuota poggia sulla base della matrice=> � immediatamente utilizzabile*/
					agibile++;
			}else
				if (this.board[colonna+delta][riga]==giocatore){ // casella contenete pedina dello stesso seme del giocatore
					pedina ++;
				}
				else//pedina avversara
					break;
		}
		
		
		break;
	
	
	case W:
		for(int delta=1; delta<= Math.min(restanti,colonna);delta++){
			if (this.board[colonna-delta][riga]==0){
				//Verifico se la casella sottostante � piena
				if(riga>0)
					if(this.board[colonna-delta][riga-1]!=0)
						agibile++;
					else
						nonAgibile++;
				else //La casella vuota poggia sulla base della matrice=> � immediatamente utilizzabile*/
					agibile ++;
			}else
				if (colonna-delta<=lowerBound)
					break;
				if (this.board[colonna-delta][riga]==giocatore)
					pedina++;
				else//pedina avversaria
					break;
		}
		break;
		
	case NE://x/colonna: aumenta y/riga: aumenta
		for(int delta=1; delta <= Math.min(restanti,Math.min((TOT_RIGHE-riga-1),(TOT_COLONNE-1)-colonna)); delta++){
			if (this.board[colonna+delta][riga+delta]==0)
				//Verifico se la casella sottostante � piena
				if(riga+delta>0)
					if(this.board[colonna+delta][riga+delta-1]!=0)
						agibile++;
					else
						nonAgibile++;
				else
					agibile++;
			else{
				if (this.board[colonna+delta][riga+delta]==giocatore)
					pedina++;
				else//pedina avversaria
					break;
			}
		}
		break;
		
	case NW://x/colonna: diminuisce, y/riga: aumenta
		for(int delta=1; delta <= Math.min(restanti,Math.min((TOT_RIGHE-riga-1),colonna)); delta++){
			if (this.board[colonna-delta][riga+delta]==0){
				//Verifico se la casella sottostante � piena
				if(riga+delta>0)
					if(this.board[colonna-delta][riga+delta-1]!=0)
						agibile++;
					else
						nonAgibile++;
				else
					agibile++;
			}else{
				if (colonna-delta<=lowerBound)
					break;
			
				if (this.board[colonna-delta][riga+delta]==giocatore)
					pedina++;
				else//pedina avversaria
					break;
			}
		}
		break;
		
	case SE://x/colonna: aumenta, y/riga: diminuisce
		for(int delta=1; delta <= Math.min(restanti, Math.min(riga, TOT_COLONNE-colonna-1)  ); delta++){
			if(this.board[colonna+delta][riga-delta]==0)
				//Verifico se la casella sottostante � piena
				if(riga-delta>0)
					if(this.board[colonna+delta][riga-delta-1]!=0)
						agibile++;
					else
						nonAgibile++;
				else
					agibile++;
			else {
				if (colonna + delta >=lowerBound)
					break;
				if (this.board[colonna+delta][riga-delta]==giocatore)
					pedina++;
				else //pedina avversaria
					break;
			}
		}
		break;
		
	case SW://x/colonna: diminuisce, y/riga: diminuisce
		for(int delta=1; delta <= Math.min(restanti, Math.min(riga,colonna)); delta++){
			if (this.board[colonna-delta][riga-delta]==0)
				//Verifico se la casella sottostante � piena
				if(riga-delta>0)
					if(this.board[colonna-delta][riga-delta-1]!=0)
						agibile++;
					else
						nonAgibile++;
				else
					agibile++;
			else{
				if (colonna-delta<=lowerBound)
					break;
				if(this.board[colonna-delta][riga-delta]==giocatore)
					pedina++;
				else
					break;
			}
		}
		break;
	}
	
	if (pedina==1){
		if (agibile==0 && nonAgibile==0)
			return 0;
		//un vuoto agibile
		if(agibile==1){
			return AGIBILE_PEDINA;
		}
		//un vuoto non agibile
		else
			return NON_AGIBILE_PEDINA;
	}
	else{
		//due vuoti agibile
		if(agibile==2){
			return AGIBILE_AGIBILE;
		}
		//due vuoti non agibili
		else
			if (nonAgibile==2)
				return NON_AGIBILE_NON_AGIBILE;
			else
				//un vuoto agibile e uno non agibile
				if ((agibile==1)&& (nonAgibile==1))
					return AGIBLE_NON_AGIBILE;
	}
	
	
	return 0;
	
}



//Override metodi AbstractDataModel

@Override
public int getColumnCount() {
	// TODO Auto-generated method stub
	return TOT_COLONNE;
}

@Override
public int getRowCount() {
	// TODO Auto-generated method stub
	return TOT_RIGHE;
}

@Override
public Object getValueAt(int rowIndex, int columnIndex) {
	//Occorre gestire l'inversione tra riche e colonne
	//Inoltre le righe vanno fornite in ordine inverso per adattarsi al Layout della Table
	
	int X= columnIndex;
	int Y= TOT_RIGHE-rowIndex-1;
	//return randomGenerator.nextInt();
	return this.board[X][Y];
	//////System.out.println("Richiesta: " + rowIndex + " " + columnIndex);
	//return 0;
}


//GETTERs & SETTERs
public int[][] getBoard() {
	return board;
}

public void setBoard(int[][] board) {
	this.board = board;
}

public int getTurno() {
	return turno;
}

public void setTurno(int turno) {
	this.turno = turno;
}

//Metodi privati
private void switchTurno(){
	this.turno= this.turno * (-1);
}

//Costruttori
public Stato(){
	board = new int[TOT_COLONNE][TOT_RIGHE+1]; //Aggiungo una riga per conservare l'indice di riga dell'ultimo elemento inserito per ogni colonna
        this.reset();
}


public void reset(){

    //Reset game board
    for (int x=0;x<TOT_COLONNE;x++)
        for(int y=0;y<TOT_RIGHE;y++)
            this.board[x][y]=0;

    //Reset top array
    for (int x=0; x<TOT_COLONNE;x++)
        this.board[x][TOT_RIGHE]=-1;

    //Reset turno
    this.turno=1;

    //Reset euristica;
    this.heuristic=-3000;

    //Reset lastAction
    this.lastActionPerformend=-1;
}

//Metodi usati come utilità o strumenti di DEBUG durante lo sviluppo (Non verranno usati nella versione finale)
public void stampa(){
	for(int y=TOT_RIGHE-1; y>=0; y--){
		for(int x=0; x<TOT_COLONNE; x++){
			if (board[x][y]==1)
			System.out.print(" X ");
			else 
				if (board[x][y]==-1)
					System.out.print(" O ");
				else 
					System.out.print(" - ");
		}
		System.out.println();
	}
}

private int verticalHeuristic(int giocatore, int colonna){
	int top = (this.board[colonna][TOT_RIGHE] ); // recupero l'indice dell'ultima pedina inserita.
	int sum=0;
	top++; // Parto dalla prima pedina vuota
	int count=0;
	int j=1;
	boolean stop=false;
	//controllo le 3 pedine sottostanti 
	while (j<4 && (top-j>=0) && !stop){
		//se la pedina � di del giocatore
		if (this.board[colonna][top-j]==giocatore){
			count++;
			j++;
		}
		//la pedina � dell'avversario. Non � possibile che ci siano pedine VUOTE!
		else{
			//E' inutile proseguire
			stop=true;
		}
	}

	if (8-top>=(4-count)){
		sum++;
	}
	return sum;
}

private int horizontalHeuristic(int giocatore, int colonna){
	int top = (this.board[colonna][TOT_RIGHE] ); // recupero l'indice dell'ultima pedina inserita.
	//int sum=0;
	int count=1;
	int j;
	boolean stop;
	//boolean agibile =false;
	//boolean nonagibile = false;
	top++; //mi sposto sulla prima posizione vuota della colonna
	
	/*
	 * controllo 3 pedine a DX
	 */
	j=1;
	stop=false; 
	while ((j<4) && ((colonna+j) < TOT_COLONNE) && !stop){
		//se la pedina � di @giocatore
		if (this.board[colonna+j][top]==giocatore){
			count++;
			//agibile=true;
		} 
		else
			//la casella � vuota 
			if ((this.board[colonna+j][top]==0)){
				//la casella � anche la prima non vuota della colonna.
				//quindi � agibile
				if ((top>0)&& (this.board[colonna+j][top-1]!=0)){
					//agibile=true;
					count++;
				}
				else{
					//la casella vuota � della riga 0.Non esistono caselle sottostanti.
					//quindi � agibile
					if(top ==0){
						//agibile=true;
						count++;
					}
					//la casella e' vuota ma non e' agibile.
					else{
						/*count++;
						nonagibile = true;*/
                                                stop=true;
					}
				}
				
			}
		//la casella � dell'avversario oppure 
		else
			//E' inutile proseguire
			stop=true;
		j++;
	}
	
	/*
	 * controllo 3 pedine a SX
	 */
	j=1;
	stop=false; 
	while ((j<4) && ((colonna-j)>=0) && !stop){
		//se la pedina � di @giocatore
		if (this.board[colonna-j][top]==giocatore){
			count++;
			//agibile = true;
		} 
		else
			//la casella � vuota 
			if ((this.board[colonna-j][top]==0)){
				//la casella � anche la prima non vuota della colonna.
				//� agibile
				if ((top>0)&& (this.board[colonna-j][top-1]!=0)){
					count++;
					//agibile=true;
				}
				else{
					//la casella vuota � della riga 0.NOn esistono caselle sottostanti.
					//� agibile
					if(top ==0){
						count++;
						//agibile=true;
					}
					else{ 
						//la casella � vuota e non � agibile
						/*count++;
						nonagibile=true;*/
                                            stop=true;
					}
				}
				
			}
			
			//la casella � dell'avversario oppure � vuota ma non � la prossima che sar� riempita della colonna
		else
			//E' inutile proseguire
			stop=true;
		j++;
	}
	/*
	 * Dato il numero di caselle adiacenti orizzontalmente favorevoli si calcola il numero di
	 * tutte le possibili combinazioni di forza 4 possibili dato da (numero caselle -4)+1 
	 */
	//Se c'� almeno una casella agibile vuol dire che � possibile almeno un forza4
	//if (agibile /*&& !nonagibile*/){
	/*
		sum = (count-4)+1;
		//� possibile almeno un forza4 
		if (sum>0)
			return sum;
	} */
        if (count>3)
            return (count-4)+1;
	return 0;
}

/*																						\			
 * Conta data una colonna quanti forza4 si possono fare lungo la dagonele principale --> \
 */
private int firstDiagonalHeuristic(int giocatore, int colonna){
	int top = (this.board[colonna][TOT_RIGHE] ); // recupero l'indice dell'ultima pedina inserita.
	//int sum=0;
	int count=1;
	int j;
	boolean stop;
	//boolean agibile = false;
	//boolean nonagibile = false;
	top++; //mi sposto sulla prima posizione vuota della colonna
	
	/*
	 * controllo 3 pedine a in basso a DX
	 */
	j=1;
	stop=false; 
	while ((j<4) && ((colonna+j) < TOT_COLONNE)&&(top-j >=0) && !stop){
		//se la pedina � di @giocatore
		if (this.board[colonna+j][top-j]==giocatore){
			count++;
            		//agibile = true;
		} 
		else
			//la casella � vuota 
			if ((this.board[colonna+j][top-j]==0)){
				//la casella � anche la prima non vuota della colonna.
				//� agibile
				if ((top-j>0)&& (this.board[colonna+j][top-j-1]!=0)){
					count++;
					//agibile = true;
				}
				else{
					//la casella vuota � della riga 0.NOn esistono caselle sottostanti.
					//la casella � agibile
					if(top-j ==0){
						count++;
						//agibile = true;
					}
					//la casella � vuota e non agibile.
					else {
						/*count++;
						nonagibile = true;*/
                                                stop=true;
					}
				}
				
			}
		//la casella � dell'avversario oppure � vuota ma non � la prossima che sar� riempita della colonna
		else
			//E' inutile proseguire
			stop=true;
		j++;
	}
	/*
	 * controllo 3 pedine a SX
	 */
	j=1;
	stop=false; 
	while ((j<4) && ((colonna-j)>=0)&& (top +j < TOT_RIGHE) && !stop){
		//se la pedina � di @giocatore
		if (this.board[colonna-j][top+j]==giocatore){
			count++;
			//agibile = true;
		} 
		else
			//la casella � vuota 
			if (this.board[colonna-j][top+j]==0){
				//la casella � vuota ma ha caselle piene sottostanti -> � agibile
				if (this.board[colonna-j][top+j-1]!=0){
					count++;
					//agibile = true;
				}
				else{
					//la casella � vuota ma non ha caselle sottostanti quindi non � agibile
					/*count++;
					nonagibile = true;*/
                                        stop=true;
				}
			}
			//la casella � dell'avversario oppure � vuota ma non � la prossima che sar� riempita della colonna
		else
			//E' inutile proseguire
			stop=true;
		j++;
	}
	
	//Se c'� almeno una casella agibile vuol dire che c'� almeno un forza 4 accessibile da questa locazione
	//if (agibile /*&& !nonagibile*/){
	/*
		sum = (count-4)+1;
		if (sum>0)
			return sum ;
	} */
        if (count>3)
            return (count-4)+1;
	return 0;
}

/*																						 /			
 * Conta data una colonna quanti forza4 si possono fare lungo la dagonele secondaria -->/
 */
private int secondDiagonalHeuristic(int giocatore, int colonna){
	int top = (this.board[colonna][TOT_RIGHE] ); // recupero l'indice dell'ultima pedina inserita.
	//int sum=0;
	int count=1;
	int j;
	boolean stop;
	//boolean agibile = false;
	//boolean nonagibile = false;
	top++; //mi sposto sulla prima posizione vuota della colonna
	
	/*
	 * controllo 3 pedine a in basso a SX
	 */
	j=1;
	stop=false; 
	while ((j<4) && ((colonna-j)>=0)&&(top-j >=0) && !stop){
		//se la pedina � di @giocatore
		if (this.board[colonna-j][top-j]==giocatore){
			count++;
			//agibile = true;
		} 
		else
			//la casella � vuota 
			if ((this.board[colonna-j][top-j]==0)){
				//la casella � anche la prima non vuota della colonna.
				//� agibile
				if ((top-j>0)&& (this.board[colonna-j][top-j-1]!=0)){
					count++;
					//agibile = true;
				}
				else{
					//la casella vuota � della riga 0.NOn esistono caselle sottostanti.
					//la casella � agibile
					if(top-j ==0){
						count++;
						//agibile = true;
					}
					//la casella � vuota e non agibile.
					else {
						/*count++;
						nonagibile = true;*/
                                            stop=true;
					}
				}
				
			}
		//la casella � dell'avversario oppure � vuota ma non � la prossima che sar� riempita della colonna
		else
			//E' inutile proseguire
			stop=true;
		j++;
	}
	/*
	 * controllo 3 pedine in alto a DX
	 */
	j=1;
	stop=false; 
	while ((j<4) && ((colonna+j)<TOT_COLONNE)&& (top +j < TOT_RIGHE) && !stop){
		//se la pedina � di @giocatore
		if (this.board[colonna+j][top+j]==giocatore){
			count++;
			//agibile = true;
		} 
		else
			//la casella � vuota 
			if (this.board[colonna+j][top+j]==0){
				//la casella � vuota ma ha caselle piene sottostanti -> � agibile
				if (this.board[colonna+j][top+j-1]!=0){
					count++;
					//agibile = true;
				}
				else{
					//la casella � vuota ma non ha caselle sottostanti quindi non � agibile
					/*count++;
					nonagibile = true;*/
                                    stop=true;
				}
			}
			//la casella � dell'avversario oppure � vuota ma non � la prossima che sar� riempita della colonna
		else
			//E' inutile proseguire
			stop=true;
		j++;
	}
	
	//Se c'� almeno una casella agibile vuol dire che c'� almeno un forza 4 accessibile da questa locazione
	//if (agibile /*&& !nonagibile*/){
		/*sum = (count-4)+1;
		if (sum>0)
			return sum ;
	} */
        if (count>3)
            return (count-4)+1;
	return 0;
}

private int euristicaFutura(){
	int tot=0;
	
	int sumX=0;
	int sumY=0;
	int x,y;
	for (int i=0;i<TOT_COLONNE;i++){
		
		// CONTROLLO VERTICALE PER MAX 
		 x = this.verticalHeuristic(1, i);
		 //////System.out.println("colonna :"+i+"  Max Verticale : "+x);
		 
		 sumX +=x;
		 
		//CONTROLLO VERTICALE PER MIN
		 y = this.verticalHeuristic(-1, i);
		 //////System.out.println("colonna :"+i+"  min Verticale : "+y);
		 
		 sumY +=y;
		 
		// CONTROLLO ORIZONTALE PER MAX 
		 x = this.horizontalHeuristic(1, i);
		 //////System.out.println("colonna :"+i+"  Max orizzontale : "+x);
		 
		 sumX +=x;
		 
		//CONTROLLO ORIZONTALE PER MIN
		 y = this.horizontalHeuristic(-1, i);
		 //////System.out.println("colonna :"+i+"  min orizzontale : "+y);
		 
		 sumY +=y;
		
		// CONTROLLO DIAGONALE PRINCIPALE PER MAX 
		 x = this.firstDiagonalHeuristic(1, i);
		 //////System.out.println("colonna :"+i+"  Max diagonale Principale : "+x);
		 
		 sumX +=x;
		 
		//CONTROLLO DIAGONALE PRINCIPALE PER MIN
		 y = this.firstDiagonalHeuristic(-1, i);
		 //////System.out.println("colonna :"+i+"  min diagonale Principale : "+y);
		 
		 sumY +=y;
		 
		// CONTROLLO DIAGONALE SECONDARIA PER MAX 
		 x = this.secondDiagonalHeuristic(1, i);
		 //////System.out.println("colonna :"+i+"  Max diagonale Secondaria : "+x);
		 
		 sumX +=x;
		 
		//CONTROLLO DIAGONALE SECONDARIA PER MIN
		 y = this.secondDiagonalHeuristic(-1, i);
		 //////System.out.println("colonna :"+i+"  min diagonale Secondaria : "+y);
		 
		 sumY +=y;
		 
		 //se entrambe le somme parziali sono uguali a 0 non ci sono nuove mosse
		 if (sumX==0 && sumY==0)
			 tot = END_GAME;
		 else
			 tot =(sumX - sumY);
	}
	return tot;

}




public int getHeuristic(){
	
	if(this.heuristic == this.NOT_DEF){
		
		int presente = this.euristicaPresente();
		
		if (presente == this.MAX_WIN || presente == this.MIN_WIN)
			this.heuristic =  presente;
		else{
			int futuro = this.euristicaFutura();
			if (futuro == this.END_GAME)
				this.heuristic=futuro;
			else
				this.heuristic = futuro+ (presente*10);
		}
			
	}
	
	return this.heuristic;
}


//Verifica se � possibile applicare allo stato l'azione specificata
public boolean azioneConsentita(int action){
	
	//Controllo che l'azione sia compresa tra 1 e TOT_COLONNE
	if (action>0 && action<=TOT_COLONNE)
		//NB: Le azioni vanno da 1 a TOT_COLONNE, gli indici di colonna vanno da 0 a TOT_COLONNE -1
		if (getTop(action-1)< (TOT_RIGHE -1))//TOT_RIGHE -1 perch� ho bisogno che ci sia almeno una casella vuota
			return true;
	
	return false;
}

private int getTop(int aColumn){
	return this.board[aColumn][TOT_RIGHE];
}




}
