package se.claremont.taftestlinkadapter.webpages;

/**
 * Shared template page for displaying errors
 *
 * Created by jordam on 2017-03-21.
 */
public class ErrorPage {

    private static String LF = System.lineSeparator();

    @SuppressWarnings("StringBufferReplaceableByString")
    public static String toHtml(String content){
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>").append(System.lineSeparator());
        sb.append("<html lang=\"en\">").append(System.lineSeparator());
        sb.append("   <head>").append(System.lineSeparator());
        sb.append(CommonSections.headSection("", "", "")).append(System.lineSeparator());
        sb.append("   </head>").append(System.lineSeparator());
        sb.append("   <body>").append(System.lineSeparator());
        sb.append("    <table id=\"CONTENT\">").append(LF).append(LF);
        sb.append("      <tr>").append(LF);
        sb.append("        <td>").append(LF).append(LF);
        sb.append(CommonSections.pageHeader()).append(System.lineSeparator());
        sb.append("      <h1>Oups... Something went wrong... :/</h1>").append(System.lineSeparator());
        sb.append(content);
        sb.append(     CommonSections.pageFooter());
        sb.append("        </td>").append(LF);
        sb.append("      </tr>").append(LF).append(LF);
        sb.append("    </table>").append(LF).append(LF);
        sb.append("</html>").append(System.lineSeparator());
        return sb.toString();
    }
}
