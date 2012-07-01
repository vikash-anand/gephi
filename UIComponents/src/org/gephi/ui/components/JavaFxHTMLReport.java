/*
 Copyright 2008-2012 Gephi
 Authors : VIKASH ANAND <vikash.anand@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2012 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.ui.components;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import org.apache.commons.codec.binary.Base64;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vikash Anand
 */
class ReportSelection implements Transferable {

    private static ArrayList flavors = new ArrayList();

    static {
        try {
            flavors.add(new DataFlavor("text/html;class=java.lang.String"));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    private String html;

    /**
     *
     * @param html
     */
    public ReportSelection(String html) {
        this.html = html;
        String newHTML = new String();
        String[] result = html.split("file:");
        boolean first = true;
        for (int i = 0; i < result.length; i++) {
            if (result[i].contains("</IMG>")) {
                String next = result[i];
                //System.out.println(">  " + next);
                String[] elements = next.split("\"");
                String filename = elements[0];


                ByteArrayOutputStream out = new ByteArrayOutputStream();

                File file = new File(filename);
                try {
                    BufferedImage image = ImageIO.read(file);
                    ImageIO.write((RenderedImage) image, "PNG", out);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte[] imageBytes = out.toByteArray();
                String base64String = Base64.encodeBase64String(imageBytes);
                if (!first) {

                    newHTML += "\"";
                }
                first = false;
                newHTML += "data:image/png;base64," + base64String;
                for (int j = 1; j < elements.length; j++) {
                    newHTML += "\"" + elements[j];
                }
            } else {
                newHTML += result[i];
            }
        }
        this.html = newHTML;
    }

    /**
     *
     * @return
     */
    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) flavors.toArray(new DataFlavor[flavors.size()]);
    }

    /**
     *
     * @param flavor
     * @return
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    /**
     *
     * @param flavor
     * @return
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (String.class.equals(flavor.getRepresentationClass())) {
            return html;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}

public class JavaFxHTMLReport extends javax.swing.JDialog {

    /**
     * Creates new form JavaFxHTMLReport
     */
    private String mHTMLReport;

    public JavaFxHTMLReport(java.awt.Frame parent, String html) {
        super(parent, false);
        mHTMLReport = html;
        initComponents();
        final JFXPanel fxPanel = new JFXPanel();
        browserPanel.add(fxPanel);
        Dimension dimension = new Dimension(700, 600);
        setPreferredSize(dimension);
        setTitle("JavaFx HTML Report");
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        Platform.setImplicitExit(false);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                initFX(fxPanel, mHTMLReport);
            }
        });
    }

    private static void initFX(JFXPanel fxPanel, String mHTMLReport) {
        Scene scene = createScene(mHTMLReport);
        fxPanel.setScene(scene);
    }

    private static Scene createScene(String mHTMLReport) {
        Browser browser = new Browser(mHTMLReport);
        Scene scene = new Scene(browser, 700, 500, Color.web("#666970"));
        scene.getStylesheets().add("webviewsample/BrowserToolbar.css");
        return (scene);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        printButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        browserPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        buttonPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        printButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/print.png"))); // NOI18N
        printButton.setText(org.openide.util.NbBundle.getMessage(JavaFxHTMLReport.class, "JavaFxHTMLReport.printButton.text")); // NOI18N
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        copyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/copy.gif"))); // NOI18N
        copyButton.setText(org.openide.util.NbBundle.getMessage(JavaFxHTMLReport.class, "JavaFxHTMLReport.copyButton.text")); // NOI18N
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/components/resources/save.png"))); // NOI18N
        saveButton.setText(org.openide.util.NbBundle.getMessage(JavaFxHTMLReport.class, "JavaFxHTMLReport.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        closeButton.setText(org.openide.util.NbBundle.getMessage(JavaFxHTMLReport.class, "JavaFxHTMLReport.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(printButton)
                .addGap(18, 18, 18)
                .addComponent(copyButton)
                .addGap(18, 18, 18)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(printButton)
                    .addComponent(copyButton)
                    .addComponent(saveButton)
                    .addComponent(closeButton))
                .addContainerGap())
        );

        browserPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        browserPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(browserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(browserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            toolkit.getSystemClipboard().setContents(new ReportSelection(this.mHTMLReport), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_copyButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
    }//GEN-LAST:event_printButtonActionPerformed
    private final String LAST_PATH = "SimpleHTMLReport_Save_Last_Path";
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        final String html = this.mHTMLReport;

        final String path = NbPreferences.forModule(JavaFxHTMLReport.class).get(LAST_PATH, null);
        JFileChooser fileChooser = new JFileChooser(path);
        int result = fileChooser.showSaveDialog(WindowManager.getDefault().getMainWindow());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File destinationFolder = fileChooser.getSelectedFile();
            NbPreferences.forModule(JavaFxHTMLReport.class).put(LAST_PATH, destinationFolder.getAbsolutePath());
            Thread saveReportThread = new Thread(new Runnable() {

                public void run() {
                    try {
                        saveReport(html, destinationFolder);
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaFxHTMLReport.class, "SimpleHTMLReport.status.saveSuccess", destinationFolder.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "SaveReportTask");
            saveReportThread.start();

        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void saveReport(String html, File destinationFolder) throws IOException {
        if (!destinationFolder.exists()) {
            destinationFolder.mkdir();
        }

        //Find images location
        String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
        Pattern pattern = Pattern.compile(imgRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        StringBuffer replaceBuffer = new StringBuffer();
        while (matcher.find()) {
            String fileAbsolutePath = matcher.group(1);
            if (fileAbsolutePath.startsWith("file:")) {
                fileAbsolutePath = fileAbsolutePath.replaceFirst("file:", "");
            }
            File file = new File(fileAbsolutePath);
            if (file.exists()) {
                copy(file, destinationFolder);
            }

            //Replace temp path
            matcher.appendReplacement(replaceBuffer, "<IMG SRC=\"" + file.getName() + "\">");
        }
        matcher.appendTail(replaceBuffer);

        //Write HTML file
        File htmlFile = new File(destinationFolder, "report.html");
        FileOutputStream outputStream = new FileOutputStream(htmlFile);
        OutputStreamWriter out = new OutputStreamWriter(outputStream, "UTF-8");
        out.append(replaceBuffer.toString());
        out.flush();
        out.close();
        outputStream.close();
    }

    public void copy(File source, File dest) throws IOException {
        FileChannel in = null, out = null;
        try {
            if (dest.isDirectory()) {
                dest = new File(dest, source.getName());
            }
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();

            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);

            out.write(buf);

        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel browserPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton copyButton;
    private javax.swing.JButton printButton;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}

class Browser extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public Browser(String mHTMLReport) {
        getStyleClass().add("browser");
        webEngine.loadContent(mHTMLReport);
        System.out.println(mHTMLReport);
        getChildren().add(browser);

    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 500;
    }
}
