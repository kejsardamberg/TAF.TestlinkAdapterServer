package se.claremont.taftestlinkadapter.webpages;

/**
 * Template for info web pages
 *
 * Created by jordam on 2017-03-21.
 */
public class InfoPage {

    private static String LF = System.lineSeparator();

    /**
     * The page HTML generator.
     *
     * @param content The main content to apply this template upon
     * @return Returns the HTML code for the page.
     */
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
        sb.append(content);
        sb.append(     CommonSections.pageFooter());
        sb.append("        </td>").append(LF);
        sb.append("      </tr>").append(LF).append(LF);
        sb.append("    </table>").append(LF).append(LF);
        sb.append("</html>").append(System.lineSeparator());
        return sb.toString();
    }
}
