package gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetection extends javax.swing.JFrame {

    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;
    Mat frame = new Mat();
    boolean outputRejectLevels = true;
    MatOfDouble scaleFactor1 = new MatOfDouble(1.3);
    MatOfDouble scaleFactor2 = new MatOfDouble(1.3);
    MatOfByte mem = new MatOfByte();
    MatOfInt minNeighbors1 = new MatOfInt(4);
    MatOfInt minNeighbors2 = new MatOfInt(4);

    int sumFront = 0;
    int sumSide = 0;
    int found = 0;
    int notFound = 0;
    CascadeClassifier frontalDetector = new CascadeClassifier(FaceDetection.class.getResource("haarcascade_frontalface_alt2.xml").getPath());
    CascadeClassifier sideDetector = new CascadeClassifier(FaceDetection.class.getResource("haarcascade_profileface.xml").getPath());
    MatOfRect frontalDetection = new MatOfRect();
    MatOfRect sideDetection = new MatOfRect();

    class DaemonThread implements Runnable {

        protected volatile boolean runnable = false;
        private int frameNum = 0;
        private List<Rect> faces = new ArrayList<>();

        @Override
        public void run() {
            synchronized (this) {
                while (runnable) {
                    if (webSource.grab()) {
                        webSource.retrieve(frame);
                        if (frameNum > 15) {
                            faces = detect();
                            System.out.println("detection!");
                            frameNum = 0;
                        }
                        for (Rect rect : faces) {

                            Core.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                                    new Scalar(255, 0, 0));
                        }
//                        System.out.println("frame num: " + frameNum);
                        frameNum++;
                        //show result frame                        
                        try {
                            Highgui.imencode(".png", frame, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                            BufferedImage buff = (BufferedImage) im;
                            jPanel1.getGraphics().drawImage(buff, 0, 0, getWidth(), getHeight(), 0, 0, buff.getWidth(), buff.getHeight(), null);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.out.println("Error");
                        }
                    }
                }
            }
        }

        private List<Rect> detect() {
            long start = System.nanoTime();
            frontalDetector.detectMultiScale(frame, frontalDetection, minNeighbors1, scaleFactor1);
            long end = System.nanoTime();
//            System.out.println("time passed: " + (end - start) / 1000000);

            for (Rect rect : frontalDetection.toArray()) {
                System.out.println("found");
                Core.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 0, 255));
            }
            if (frontalDetection.toArray().length > 0) {
                sumFront = sumFront + 1;
                System.out.println("Positive frontal faces :" + sumFront);
            }
            sideDetector.detectMultiScale(frame, sideDetection, minNeighbors2, scaleFactor2);
            if (sideDetection.toArray().length > 0) {
                sumSide = sumSide + 1;
                System.out.println("Side faces :" + sumSide);
            }
            for (Rect rect : sideDetection.toArray()) {
                System.out.println("found side");
                Core.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 255));
            }

            if (frontalDetection.toArray().length > 0 || sideDetection.toArray().length > 0) {
                String filename = "/home/giannis/Downloads/DETECTED/" + new Date().toString() + ".png";
                Highgui.imwrite(filename, frame);
                found++;
                System.out.println("found: " + found);
            } else {
                notFound++;
                System.out.println("not found: " + notFound);
            }
            ArrayList<Rect> facesDetected = new ArrayList<Rect>();
            facesDetected.addAll(frontalDetection.toList());
            facesDetected.addAll(sideDetection.toList());
            for (Rect mayBeFace : facesDetected) {
                Mat submat = frame.submat(mayBeFace);
                MatOfRect faceDetections1 = new MatOfRect();
                MatOfRect faceDetections2 = new MatOfRect();
                frontalDetector.detectMultiScale(submat, faceDetections1, minNeighbors1, scaleFactor1);
                sideDetector.detectMultiScale(submat, faceDetections2, minNeighbors2, scaleFactor2);
                if (faceDetections1.toArray().length + faceDetections2.toArray().length > 0) {
                    String filename = "/home/giannis/Downloads/TRUE_DETECTED/" + new Date().toString() + ".png";
                    Highgui.imwrite(filename, submat);
                } else {
                    String filename = "/home/giannis/Downloads/FALSE_DETECTED/" + new Date().toString() + ".png";
                    Highgui.imwrite(filename, submat);
                }

            }
            return facesDetected;
        }
    }

    public FaceDetection() {
        initComponents();
        webSource = new VideoCapture(0);// video capture from default cam
        myThread = new DaemonThread();//create object of threat class
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();//start thread
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1205, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 670, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FaceDetection.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FaceDetection.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FaceDetection.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FaceDetection.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                FaceDetection faceDetection = new FaceDetection();
                faceDetection.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
