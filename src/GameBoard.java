

import f4.stato.Stato;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;


public class GameBoard extends JTable{

	int DEFAULT_CELL_SIZE=40;
	Stato statoModel;
	
	//Classe per la gestione dell'aspetto grafico della scacchiera di gioco
	class GameBoardRenderer extends DefaultTableCellRenderer 
	{
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	    {
	        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        
	        if (value.equals(1))
	        	c.setBackground(new java.awt.Color(255,000,000));//Rosso
	        else if(value.equals(-1))
	        	c.setBackground(new java.awt.Color(255,255,000));//Giallo
	        else
	        	c.setBackground(new java.awt.Color(220,220,220)); //Bianco
	        return c;
	    }
	}	
	
	
	//Imposta le dimensioni di ogni cella(quadrate!!)
	public void setCellSize(int size){
		this.setRowHeight(size);
    	TableColumn column = null;
        //this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    	for (int i = 0; i < this.getColumnCount(); i++) {
    	    column = this.getColumnModel().getColumn(i);
    	    column.setPreferredWidth(size);
    	    
    	}
	}
	
	
	//Costruttori
	public GameBoard(Stato statoAssociato){
		//statoAssociato � lo stato sorgente per la tabella
		//Il contenuto della GameBoard si adatter� automaticamente alle modifiche dello stato
		//this.statoModel= statoAssociato;
		this.setModel(statoAssociato);
		this.setDefaultRenderer(Object.class, new GameBoardRenderer());
		this.setCellSize(DEFAULT_CELL_SIZE);
	}
	
	public GameBoard(){
		//statoAssociato � lo stato sorgente per la tabella
		//Il contenuto della GameBoard si adatter� automaticamente alle modifiche dello stato
		this.setDefaultRenderer(Object.class, new GameBoardRenderer());
		this.setCellSize(DEFAULT_CELL_SIZE);
	}

        public void associaStato(Stato mioStato){
            this.setModel(mioStato);
        }


	
	
	
	
}
