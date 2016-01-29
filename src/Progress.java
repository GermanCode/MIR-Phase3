

import javax.swing.JLabel;

public class Progress implements Runnable{
	
	public static int SLEEP_TIME = 1000;
	
	private Progressable p;
	private JLabel label;
	
	public Progress(Progressable p, JLabel label) {
		this.p = p;
		this.label = label;
	}
	
	@Override
	public void run() {
		Integer seconds = 0;
		while (true){
			try{
				label.setVisible(false);
				String x = p.getProgress();
				boolean terminate = false;
				if (x == null){
					x = "done";
					terminate = true;
				}
				label.setText(x +  " (" + seconds.toString() + " sec)");
				label.setVisible(true);
				Thread.sleep(SLEEP_TIME);
				if (terminate)
					return;
				seconds++;
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
