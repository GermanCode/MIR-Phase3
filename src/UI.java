import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class UI {

	public static int FRAME_WIDTH = 800;
	public static int FRAME_HEIGHT= 600;
	
	public static int LABEL_WIDTH = 100;
	public static int FIELD_WIDTH = 400;
	public static int BUTTON_WIDTH= 80;
	public static int ROW_HEIGHT = 24;
	public static int GAP_HEIGHT = 10;
	
	
	public static JTextField indegreeField = new JTextField();
	public static JTextField outdegreeField = new JTextField();
	public static JTextField initUrlField = new JTextField();
	public static JTextField numberOfDocsField = new JTextField();
	public static JButton addUrlButton = new JButton();
	public static JButton crawlButton = new JButton();
	public static ArrayList<String> urls = new ArrayList<String>();
	
	
	public static JPanel addOneRow (String label, JTextField field, JButton button, String buttonLabel){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		if (label == null)
			label = "";
		{
			JLabel jlabel = new JLabel(label);
			jlabel.setPreferredSize(new Dimension(LABEL_WIDTH, ROW_HEIGHT));
			jlabel.setMinimumSize(jlabel.getPreferredSize());
			jlabel.setMaximumSize(jlabel.getPreferredSize());
			panel.add(jlabel);
		}
			
		if (field != null){
			field.setPreferredSize(new Dimension(FIELD_WIDTH, ROW_HEIGHT));
			field.setMinimumSize(field.getPreferredSize());
			field.setMaximumSize(field.getPreferredSize());
			panel.add(field);
		}else{
			JLabel jlabel = new JLabel("");
			jlabel.setPreferredSize(new Dimension(FIELD_WIDTH, ROW_HEIGHT));
			jlabel.setMinimumSize(jlabel.getPreferredSize());
			jlabel.setMaximumSize(jlabel.getPreferredSize());
			panel.add(jlabel);			
		}
		if (button != null){
			button.setPreferredSize(new Dimension(BUTTON_WIDTH, ROW_HEIGHT));
			button.setMinimumSize(button.getPreferredSize());
			button.setMaximumSize(button.getPreferredSize());
			button.setText(buttonLabel);
			panel.add(button);
		}else{
			JLabel jlabel = new JLabel("");
			jlabel.setPreferredSize(new Dimension(BUTTON_WIDTH, ROW_HEIGHT));
			jlabel.setMinimumSize(jlabel.getPreferredSize());
			jlabel.setMaximumSize(jlabel.getPreferredSize());
			panel.add(jlabel);					
		}
		return panel;
	}
	
	public static JPanel getGap(){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JLabel jlabel = new JLabel("");
		jlabel.setPreferredSize(new Dimension(LABEL_WIDTH, GAP_HEIGHT));
		jlabel.setMinimumSize(jlabel.getPreferredSize());
		jlabel.setMaximumSize(jlabel.getPreferredSize());
		panel.add(jlabel);
		return panel;
	}
	
    public static void main(String[] args) {
    	JFrame frame = new JFrame();
    	frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
    	frame.getContentPane().add(getGap());
    	frame.getContentPane().add(getCrawlingPanel());
    	frame.getContentPane().add(getGap());
    	frame.getContentPane().add(getSearchPanel());
    	frame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    	frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
    	frame.setResizable(false);
    	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    	frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
    	frame.pack();
    	frame.setVisible(true);
    }
    
    public static JPanel getSearchPanel (){
    	JPanel panel = new JPanel(); 
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(addOneRow("title weight", new JTextField(), null, null));
    	panel.add(addOneRow("abstract weight", new JTextField(), null, null));
    	panel.add(addOneRow("authors weight", new JTextField(), null, null));
    	panel.add(addOneRow("query", new JTextField(), new JButton(), "search"));
    	panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Searching"),
                BorderFactory.createEmptyBorder()));
    	return panel;    	
    }
    
    public static JPanel getCrawlingPanel (){
    	JPanel panel = new JPanel(); 
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(addOneRow("init url", initUrlField, addUrlButton, "Add"));
    	panel.add(addOneRow("indegree", indegreeField, null, null));
    	panel.add(addOneRow("outdegree", outdegreeField, null, null));
    	panel.add(addOneRow("number of docs", numberOfDocsField, crawlButton, "Crawl"));
    	panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Crawling"),
                BorderFactory.createEmptyBorder()));
    	addUrlButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				urls.add(UI.initUrlField.getText());
				for (String url: urls)
					System.err.println(url);
			}
		});
    	final JLabel ali = new JLabel();
    	ali.setText(" ");
    	panel.add(ali);
    	
    	crawlButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int numberOfDocs = Integer.parseInt(UI.numberOfDocsField.getText());
				int indegree = Integer.parseInt(UI.indegreeField.getText());
				int outdegree = Integer.parseInt(UI.outdegreeField.getText());
				Crawler crawler = new Crawler(numberOfDocs, indegree, outdegree, urls);
				crawler.start();
				Progress x = new Progress(crawler, ali);
				new Thread(x).start();
			}
		});
    	return panel;
    }
}
