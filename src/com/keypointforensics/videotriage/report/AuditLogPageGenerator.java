package com.keypointforensics.videotriage.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.keypointforensics.videotriage.audit.CaseAuditor;

public class AuditLogPageGenerator {

    private final String NOTES_PAGE_FILENAME = "auditlog.html";

    private String mReportPageHeader;
    private String mReportRoot;

    private String mAuditFilePath;
    private String mPageName;

    private String mAuditFolderPath;

    private StringBuilder mPageBuilder;

    public AuditLogPageGenerator() {
        mPageBuilder = new StringBuilder(2000);

        mAuditFilePath = CaseAuditor.getCaseLogPath();
    }

    public void setReportPageHeader(final String reportPageHeader) {
        mReportPageHeader = reportPageHeader;
    }

    public void setReportRoot(final String reportRoot) {
        mReportRoot = reportRoot;
    }

    public void buildPage() {
        mPageBuilder.append(mReportPageHeader);

        mPageBuilder.append("<table class=\"table-fill\"><thead><tr><th>Audit Log</th></tr></thead><tbody class=\"table-hover\">");

        //
        mPageBuilder.append("<tr class=\"outer\"><td class=\"text-left\">");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(mAuditFilePath)))) {
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                mPageBuilder.append(line);
                mPageBuilder.append("<br>");
            }
        } catch (FileNotFoundException fileNotFoundException) {
            //fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            //ioException.printStackTrace();
        }
        mPageBuilder.append("</td></tr>");
        //

        mPageBuilder.append("</tbody></table><br><div align=\"center\"><a href=\"");
        mPageBuilder.append(mReportRoot);
        mPageBuilder.append("\">Home</a></div></body><html>");

        PrintWriter pageWriter = null;

        File statisticsPageFile = new File(mPageName);

        try {
            pageWriter = new PrintWriter(statisticsPageFile);

            pageWriter.append(mPageBuilder.toString());

            pageWriter.flush();
            pageWriter.close();
        } catch (FileNotFoundException fileNotFoundException) {
            //fileNotFoundException.printStackTrace();
        }
    }

    public void buildEmbeddablePage() {
        mPageBuilder.append("<table class=\"table-fill\"><thead><tr><th>Audit Log</th></tr></thead><tbody class=\"table-hover\">");

        //
        mPageBuilder.append("<tr class=\"outer\"><td class=\\\"text-left\\\">");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(mAuditFilePath)))) {
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                mPageBuilder.append(line);
                mPageBuilder.append("<br>");
            }
        } catch (FileNotFoundException fileNotFoundException) {
            //fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            //ioException.printStackTrace();
        }
        mPageBuilder.append("</td></tr>");
        //

        mPageBuilder.append("</tbody></table><br>");
    }

    public String getEmbeddablePage() {
        return mPageBuilder.toString();
    }

    private void setAuditLogPageName() {
        mPageName = mAuditFolderPath + NOTES_PAGE_FILENAME;
    }

    public void setAuditLogFolderPath(final String statisticsFolderPath) {
        mAuditFolderPath = statisticsFolderPath;

        setAuditLogPageName();
    }

    public String getAuditLogPageName() {
        return mPageName;
    }
}
