package f4.players;
import f4.stato.Stato;



public class HumanPlayer implements Player {
	
	
	private int nome;//1 o -1	
	private Stato stato;//Puntatore all'ambiente

        private int nextAction=-1;
        //Attributi per statistiche
        private int actionNumber=0;
        private int lastAction=-1;


        private float elaborationTime=0;
        private float lastActionElaborationTime=0;
	
	
	public HumanPlayer(Stato statoAssociato, int nome){
            this.nome=nome;
            this.stato=statoAssociato;		
	}

        

        public void setNome(int nome){
            this.nome=nome;
        }

        public void setAction(int action){
            if (this.stato.getTurno()==this.nome){
                this.nextAction = action;
            }
        }

        public void setStato(Stato mioStato){
            this.stato = mioStato;
        }
	
	public int getAction(){
                
		long startTime=System.currentTimeMillis();
		long stopTime;
                
		while(this.nextAction==-1){}
		this.actionNumber++;
                stopTime=System.currentTimeMillis();
		this.lastActionElaborationTime=stopTime-startTime;
		this.elaborationTime+=this.lastActionElaborationTime;

                //Aggiorno lastAction
                this.lastAction=nextAction;
                this.nextAction=-1;
		
		return this.lastAction;
	}
	
	

	
	
	//GETTERs & SETTERs


	
	public int getNome() {return nome;}

    @Override
    public int getLastAction() {return this.lastAction;}

    @Override
    public int getActionNumber() {return this.actionNumber;}

    @Override
    public int getComputedNodes() {return 0;}

    @Override
    public int getLastActionComputedNodes() {return 0;}

    @Override
    public int getComputedNodesAverage() {return 0;}

    @Override
    public float getElaborationTime() {return this.elaborationTime;}

    @Override
    public float getLastActionElaborationTime() {return this.lastActionElaborationTime;}

    @Override
    public float getElaborationTimeAverage() {return (this.elaborationTime/this.actionNumber);}
	
	
	
	
	
	
	

	
}
