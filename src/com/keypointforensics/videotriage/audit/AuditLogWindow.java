package com.keypointforensics.videotriage.audit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.keypointforensics.videotriage.thread.TermsVerificationThread;
import com.keypointforensics.videotriage.util.BorderUtils;
import com.keypointforensics.videotriage.util.FontUtils;
import com.keypointforensics.videotriage.util.ThreadUtils;
import com.keypointforensics.videotriage.util.Utils;
import com.keypointforensics.videotriage.util.WindowUtils;

public class AuditLogWindow extends JFrame implements ActionListener {

    private JTextArea mAuditLogTextArea;
    private JButton mDismissButton;

    public AuditLogWindow() {
        mAuditLogTextArea = new JTextArea();
        mAuditLogTextArea.setEditable(false);
        WindowUtils.setTextAreaUpdatePolicy(mAuditLogTextArea);
        JScrollPane licenseAreaScrollPane = new JScrollPane(mAuditLogTextArea);
        WindowUtils.setScrollBarIncrement(licenseAreaScrollPane);

        try (BufferedReader br = new BufferedReader(new FileReader(CaseAuditor.getCaseLogPath()))) {
            for (String line; (line = br.readLine()) != null; ) {
                mAuditLogTextArea.append(line);
                mAuditLogTextArea.append("\n");
            }
        } catch (FileNotFoundException e) {
            Utils.displayMessageDialog("Audit Log Issue", "Failed to load audit log. Error ID: 009001.");
        } catch (IOException e) {
            Utils.displayMessageDialog("Audit Log Issue", "Failed to load audit log. Error ID: 009013.");
        }
    }

    public void buildAndDisplay() {
        buildMenuBar();

        this.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderUtils.getEmptyBorder());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        mDismissButton = new JButton("Close");
        mDismissButton.setFont(FontUtils.DEFAULT_FONT);
        mDismissButton.addActionListener(this);

        buttonPanel.add(mDismissButton, BorderLayout.CENTER);

        JScrollPane termsGridPanelScrollPane = new JScrollPane(mAuditLogTextArea);
        termsGridPanelScrollPane.setPreferredSize(new Dimension(700, 400));

        contentPanel.add(termsGridPanelScrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.add(contentPanel, BorderLayout.CENTER);

        this.setTitle("Audit Log");
        this.pack();
        WindowUtils.setFrameIcon(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        WindowUtils.center(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JButton) {
            JButton button = (JButton) event.getSource();

            if (button == mDismissButton) {
                AuditLogWindow.this.dispose();
            }
        }
    }

    private void buildMenuBar() {
        JMenuBar menuBar;
        JMenu menu;

        menuBar = new JMenuBar();

        menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                new Thread() {
                    @Override
                    public void run() {
                        AuditLogWindow.this.dispose();
                    }
                }.start();
            }

        });
        menu.add(menuItem);

        this.setJMenuBar(menuBar);
    }
}
