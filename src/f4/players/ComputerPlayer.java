package f4.players;

import f4.stato.Stato;


//NB: OCCORRE MODIFICARE GLI ALGORITMI DI RICERCA IN MODO CHE EFFETTUINO IL GOALTEST SULLO STATO ATTUALE
//E NON SUI SUOI FIGLI, A TAL PROPOSITO CAMBIARE LA CHIAMATA A smartGoalTest



public class ComputerPlayer implements Player {

	//Attributi identificazione giocatore e paretri di ricerca
	private int nome;//1 o -1
	private int orizzonte;//profondita' max dell'albero.
	private String algoritmo; //Algoritmop di ricerca (minimax o alfabeta)
	
	//Stato => rappresenta l'ambiente in cui agisce il giocatore
	private Stato stato;
	
	//Attributi statistiche
        private int lastAction=-1;
        private int actionNumber=0;

        private int computedNodes=0;
        private int lastActionComputedNodes=0;

        private float elaborationTime=0;
        private float lastActionElaborationTime=0;
        
	
	
	public ComputerPlayer(int nome, int orizzonte, String algoritmo, Stato ambiente){
		this.nome=nome;
		this.orizzonte = orizzonte;
		this.setAlgoritmo(algoritmo);
		
		this.stato=ambiente;
	}
	
	
	public int getAction(){
		long startTime= System.currentTimeMillis();
                this.actionNumber++;
		if (this.algoritmo.equals("alphabeta")){
			this.lastAction= this.getMossaAlphaBetaPruning();
		}else
			this.lastAction= this.getMossaMiniMax();
		long stopTime= System.currentTimeMillis();

                //Aggiorna statistiche
		this.lastActionElaborationTime= stopTime-startTime;
		this.elaborationTime+= this.lastActionElaborationTime;
                this.computedNodes+= this.lastActionComputedNodes;
                
		return this.lastAction;
	}
	

	
	/*
	 * Dato lo stato attuale restutuisce il numero di colonna rappresentante la prossima mossa.
	 * La decisione viene presa in seguito all'esecuzione di algoritmo MINIMAX
	 * 
	 * ingresso @mioStato 
	 * uscita intero che rappresenta
	 * 							# [1..7] prossima mossa
	 * 							#-1      errore il turno non e' del giocatore 
	 * 							# 0      non esistono mosse per entrambi i giocatori
	 */	
	public int getMossaMiniMax(){
            //System.out.println("Called Minimax");
		Stato mioStato= stato;
		//Controllo se nello stato in input � effettivamente il turno del giocatore.
		//Altrimenti restituisce -1
		if (mioStato.getTurno()!=this.nome)
			return -1;
		//contatore di nodi viene generato
		this.lastActionComputedNodes = 0;
		int max=-600;
		int mossa=0;
		int x;
		Stato newStato;
		for(int i=1;i<=7;i++){
			if (mioStato.azioneConsentita(i)){
				this.lastActionComputedNodes++;
				newStato=mioStato.getSuccessore(i);
				//Controllo PATCH
				if (newStato.smartGoalTest()==(this.nome*newStato.MAX_WIN))
					return i;
				//END
				x = this.miniMax(newStato, 1);
				if (x>=max){
					max = x;
					mossa = i; 
				}
				newStato=null;
			}
		}
		if (Math.abs(max)== -mioStato.END_GAME){
			////System.out.println("Non ci sono pi� mosse. Gioco termina");
			return 0;
		}
			
		////System.out.println();
		////System.out.println("Mossa scelta da MAX : "+ mossa + "Valore euristica : "+ max);
		return mossa;
	}
	
	
	//chiamata ricorsiva dell'algoritmo minimax
	private  int miniMax(Stato nodo, int h){
		Stato newStato;
		//raggiunto il livello delle foglie. Viene calcolata l'euristica.
		if (h==orizzonte){
			int euristica = nodo.getHeuristic();
			if ((euristica >500) || euristica < -500 )
				nodo.stampa();
			////System.out.println("Livello foglie --- Valore Euristica :"+ this.nome*euristica);
			return this.nome*euristica;
		}
		//Raggiunto il goal test. Il nodo non viene espanso.
		int goal = nodo.smartGoalTest();
		if ((goal == nodo.MAX_WIN) || (goal == nodo.MIN_WIN)){
			////System.out.println("Livello: "+h+"  giocatore :  "+ nodo.getTurno()+" trovato goal: " +goal*this.nome);
			return goal*this.nome;
		}
			
		
		//Il nodo � di MAX --> deve scegliere il massimo dei valori ritornati dai figli
		if (nodo.getTurno()== this.nome){
			int max=-600;
			int x;
			for(int i=1;i<=7;i++){
				if (nodo.azioneConsentita(i)){
					newStato=nodo.getSuccessore(i);
					this.lastActionComputedNodes++;
					x = this.miniMax(newStato,h+1);
					if (x== - newStato.END_GAME) 
						x= -x;
					if (x>=max){
						max = x; 
					}
					newStato=null;
				}
			}
		////System.out.println("Livello: "+h+"  giocatore Max Euristica scelta dai figli:  " +max);
		if (Math.abs(max)==600)
			return 200;
		else
			return max;
		}
		//Il nodo � di MIN --> deve scegliere il minimo  dei valori ritornati dai figli
		else{
			int min=600;
			int x;
			for(int i=1;i<=7;i++){
				if (nodo.azioneConsentita(i)){
					newStato=nodo.getSuccessore(i);
					this.lastActionComputedNodes++;
					x = this.miniMax(newStato,h+1);
					//Lo stato in cui non ci sono mosse per min e max � negativo per entrambi quindi invertiamo il segno
					if (x==newStato.END_GAME) 
						x= -x;
					if (x<=min){
						min = x; 
					}
					newStato=null;
				}
			}
			////System.out.println("Livello: "+h+"  giocatore Min Euristica scelta dai figli:  " +min);
			if (Math.abs(min)==600)
				return -200;
			else
				return min;
		}
	}
	

	//Ricerca Alfa Beta Pruning
	public int getMossaAlphaBetaPruning(){
                //System.out.println("Called Alphabeta");
		Stato nodo=stato;
		int valMax=-600;
		int indMax=0;
		this.lastActionComputedNodes=0;
		for (int i=1; i<=7; i++){
			if (nodo.azioneConsentita(i)){
                            
                            Stato successore = nodo.getSuccessore(i);
                            this.lastActionComputedNodes++;
                            //Controllo PATCH
                            if (successore.smartGoalTest()==(this.nome*successore.MAX_WIN))
                                    return i;
                            //END

                            int val= alphaBetaMinValue(successore, -600, +600, 1);

                            if (val>=valMax){
                                    valMax=val;
                                    indMax=i;
                            }
			}
		}
		if (Math.abs(valMax)== - nodo.END_GAME){
			////System.out.println("Non ci sono pi� mosse. Gioco termina");
			return 0;
		}
		////System.out.println();
		////System.out.println("Mossa scelta da max: "+ indMax+ " Valore euristica: "+valMax);
		return indMax;
	}
	
	private int alphaBetaMaxValue(Stato nodo, int alfa, int beta, int h){
		//alfa: migliore alternativa per Max
		//beta: migliore alternativa per Min
		
		if(h==orizzonte){//nodo foglia Calcolato valore euristica�
			int euristica=  nodo.getHeuristic();
			////System.out.println("Livello Foglie ---- Valore euristica: "+this.nome*euristica);
			return this.nome*euristica;
			
		}
		
		//Raggiunto il goal test. Il nodo non viene espanso.
		int goal = nodo.smartGoalTest();
		if ((goal == nodo.MAX_WIN) || (goal == nodo.MIN_WIN)){
			////System.out.println("Livello: "+h+"  giocatore :  "+ nodo.getTurno()+" trovato goal: " +goal*this.nome);
			return goal*this.nome;
		}
		
		
		else{
			
                    int val= -600;//-infinito
                    for (int i=1; i<=7; i++){
                            if (nodo.azioneConsentita(i)){
                                    
                                    Stato successore = nodo.getSuccessore(i);
                                    this.lastActionComputedNodes++;
                                    val= Math.max(val,alphaBetaMinValue(successore, alfa, beta, h+1));

                                    //System.out.println("Beta: " + beta + " val: " + val);
                                    if (val>=beta){

                                            //System.out.println("MAX (livello " + h +"): " + val + ">=" + beta + "=> non espando!");
                                            return val;
                                    }
                                    alfa= Math.max(alfa,val);
                            }
                    }
                    ////System.out.println("Livello :"+h+ " Max ha Selezionato :"+ val);
                    if (Math.abs(val)==600)
                        return 200;
                    else
			return val;
		}
	}
	
	private int alphaBetaMinValue(Stato nodo, int alfa, int beta, int h){
		if(h==orizzonte){
			int euristica=  nodo.getHeuristic();
			////System.out.println("Livello Foglie  ---- Valore euristica: "+this.nome*euristica);
			return this.nome*euristica;
			
		}
		
			//Raggiunto il goal test. Il nodo non viene espanso.
		int goal = nodo.smartGoalTest();
		if ((goal == nodo.MAX_WIN) || (goal == nodo.MIN_WIN)){
			////System.out.println("Livello: "+h+"  giocatore :  "+ nodo.getTurno()+" trovato goal: " +goal*this.nome);
			return goal*this.nome;
		}
				
		else{
                    int val= +600;//+infinito
                    for (int i=1; i<=7; i++){
                            if (nodo.azioneConsentita(i)){
                                
                                Stato successore = nodo.getSuccessore(i);
                                this.lastActionComputedNodes++;
                                val= Math.min(val,alphaBetaMaxValue(successore, alfa, beta, h+1));
                                //System.out.println("Alpha: " + alfa+ " val: " + val);
                                if (val<=alfa){
                                        //System.out.println("MIN (livello " + h + "): " + val + "<=" + alfa + "=> non espando!");
                                        return val;
                                }
                                beta= Math.min(beta,val);
                            }
                    }
                    ////System.out.println("Livello :"+h+ " MIN ha Selezionato :"+ val);
                    if (Math.abs(val)==600)
                            return -200;
                    else
                        return val;
		}
	
	}




	
	//GETTERs & SETTERs

	public int getNome() {return nome;}
	public void setNome(int nome) {this.nome = nome;}

	public String getAlgoritmo() {return algoritmo;}
	public void setAlgoritmo(String algoritmo) {  this.algoritmo = algoritmo;}

	public int getOrizzonte() {return orizzonte;}
	public void setOrizzonte(int orizzonte) {this.orizzonte = orizzonte;}

    @Override
    public int getLastAction() {return this.lastAction;}

    @Override
    public int getActionNumber() {return this.actionNumber;}

    @Override
    public int getComputedNodes() {return this.computedNodes;}

    @Override
    public int getLastActionComputedNodes() {return this.lastActionComputedNodes;}

    @Override
    public int getComputedNodesAverage() {
       // //System.out.println("Calcolo media nodi Computer player---");
        //System.out.print("numero azioni: " + this.actionNumber);
        if (this.actionNumber==0)
            return 0;
        else
            return (this.computedNodes/this.actionNumber);
    }

    @Override
    public float getElaborationTime() {return this.elaborationTime;}

    @Override
    public float getLastActionElaborationTime() {return this.lastActionElaborationTime;}

    @Override
    public float getElaborationTimeAverage() {
        if(this.actionNumber==0)
            return 0;
        else
            return (this.elaborationTime/this.actionNumber);
    }

    @Override
    public void setAction(int action) {
        return;
    }


   
	
}
