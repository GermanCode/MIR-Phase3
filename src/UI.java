import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.print.Doc;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.gson.Gson;


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
	
	public static JTextField kmeansKField = new JTextField();
	public static JTextField kmeansThresholdField = new JTextField();
	public static JButton clusterButton = new JButton();
	
	public static JTextField pageRankAlphaField = new JTextField();
	public static JTextField pageRankThresholdField = new JTextField();
	public static JButton pageRankButton = new JButton();
	
	public static ArrayList<Document> docs;
	public static ArrayList<Point> pointDocs;
	public static Indexer indexer;
	public static ArrayList<Cluster> cluster;
	
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
	
	public static void loadAllDocs (){
		docs = new ArrayList<Document>();
		try{
			File dir = new File("Documents");
			if (dir.isDirectory() == false)
				return;
			Gson gson = new Gson();
			int counter = 0;
			File[] files = dir.listFiles();
			for (File file: files){
				counter++;
				System.err.println("processing " + counter + "/" + files.length + " : " + file.getPath());
				String json = "";
				Scanner sc = new Scanner(file);
				boolean firstLine = true;
				while (sc.hasNext()){
					if (firstLine == false)
						json+= "\n";
					json+= sc.nextLine();
					firstLine = false;
				}
				sc.close();
				try{
					Document d = gson.fromJson(json, Document.class);
					if (d != null)
						docs.add(d);
				}catch (Exception e){
					System.err.println("Failed");
				}
				if (counter > 1000)
					break;
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		indexer = new Indexer();
		System.err.println("Begin building invert index...");
//		for (Document d: docs){
//			indexer.addDoc(d);
//			for (cite c: d.cited_in){
//				for (Document d2: docs) 
//					if (d2.id.equals(c.id))
//						d2.referenceId.add(d.id);
//			}
//		}
		int counter = 0;
		for (Document d: docs){
			indexer.addDoc(d);
			for (String ref: d.reference){
				int pnt = ref.lastIndexOf("/");
				String id = "";
				pnt++;
				while (ref.charAt(pnt) != '_'){
					id+= ref.charAt(pnt);
					pnt++;
				}
				d.referenceId.add(id);;
			}
			counter++;
			System.err.println(counter + "/" + docs.size());
		}
		System.err.println("finished building invert index.");
		pointDocs = indexer.getPoints();
		System.err.println(UI.pointDocs);
	}
	
    public static void main(String[] args) {
    	loadAllDocs();
    	JFrame frame = new JFrame();
    	frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
    	frame.getContentPane().add(getGap());
    	frame.getContentPane().add(getCrawlingPanel());
    	frame.getContentPane().add(getGap());
    	frame.getContentPane().add(getSearchPanel());
    	frame.getContentPane().add(getGap());
    	frame.getContentPane().add(getKmeansPanel());
    	frame.getContentPane().add(getGap());
    	frame.getContentPane().add(getPageRankPanels());
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
    	panel.add(addOneRow("query", new JTextField(), new JButton(), "Search"));
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
    
    public static JPanel getKmeansPanel (){
    	JPanel panel = new JPanel(); 
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(addOneRow("k", kmeansKField, null, null));
    	panel.add(addOneRow("threshold", kmeansThresholdField, clusterButton, "Cluster"));
    	final JLabel ali = new JLabel();
    	ali.setText(" ");
    	panel.add(ali);
    	panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Clustering (Kmeans)"),
                BorderFactory.createEmptyBorder()));
    	clusterButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int k = Integer.parseInt(kmeansKField.getText());
				double threshold = Double.parseDouble(kmeansThresholdField.getText());
				Kmeans kmeans = new Kmeans();
				kmeans.k = k;
				kmeans.threshold = threshold;
				kmeans.points = UI.pointDocs;
				kmeans.metric = new CosineMetric();
				UI.cluster = new ArrayList<Cluster>();
				kmeans.ret = UI.cluster;
				kmeans.start();
				new Thread(new Progress(kmeans, ali)).start();
				System.err.println(UI.cluster.size());
			}
		});
    	return panel;
    }
    
    public static JPanel getPageRankPanels (){
    	JPanel panel = new JPanel(); 
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.add(addOneRow("alpha", pageRankAlphaField, null, null));
    	panel.add(addOneRow("threshold", pageRankThresholdField, pageRankButton, "Go!"));
    	final JLabel ali = new JLabel();
    	ali.setText(" ");
    	panel.add(ali);
    	panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Page Rank"),
                BorderFactory.createEmptyBorder()));
    	pageRankButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double alpha = Double.parseDouble(pageRankAlphaField.getText());
				double threshold = Double.parseDouble(pageRankThresholdField.getText());
				PageRank ranker = new PageRank();
				ranker.alpha = alpha;
				ranker.threshold = threshold;
				ranker.docs = UI.docs;
				ranker.start();
				new Thread(new Progress(ranker, ali)).start();
			}
		});
    	return panel;    	
    }
    
}

