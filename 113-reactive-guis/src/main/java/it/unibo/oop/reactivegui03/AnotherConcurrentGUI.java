//come faccio a fare il check?

package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSliderUI.ScrollListener;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 23L;

    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private static final int SLEEP_TIME = 10_000; //ms == 10s
    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");


    public AnotherConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);


        this.getContentPane().add(panel);
        this.setVisible(true);
        
        final Agent agent = new Agent();
        up.addActionListener((a) -> agent.countUp());
        down.addActionListener((a) -> agent.countDown());
        stop.addActionListener((a) -> {
            agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
        });
        new Thread(agent).start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(SLEEP_TIME);
                } catch(InterruptedException ex){
                    ex.printStackTrace();
                }
                agent.stopCounting();
                up.setEnabled(false);
                down.setEnabled(false);
                stop.setEnabled(false);
            }
        }).start();

    }

    //perché nelle soluzioni mette anche implements Serializable?
    private class Agent implements Runnable{

        private volatile Boolean stop = false;
        private int counter = 0;
        
        private volatile Boolean up = true;

        public void countUp() {
            this.up = true;
        }

        public void stopCounting() {
            this.stop = true;
            
        }

        public void countDown() {
            this.up = false;
        }

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    this.counter += up ? 1 : -1;
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
    
        }
    }
}
