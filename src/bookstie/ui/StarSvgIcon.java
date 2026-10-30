package bookstie.ui;

import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import java.awt.*;
import java.awt.geom.Path2D;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** 클래스패스 /resources/icon의 두 별 SVG에 사용된 절대 M/C/V/Z 경로를 그림. */
final class StarSvgIcon implements Icon {
    private final int width;
    private final int height;
    private final double viewWidth;
    private final double viewHeight;
    private final Path2D shape;
    private final Color color;

    StarSvgIcon(String filename, int width, int height) {
        this.width = width;
        this.height = height;
        try (InputStream input = open(filename)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Element svg = factory.newDocumentBuilder().parse(input).getDocumentElement();
            String[] viewBox = svg.getAttribute("viewBox").trim().split("[ ,]+");
            viewWidth = Double.parseDouble(viewBox[2]);
            viewHeight = Double.parseDouble(viewBox[3]);
            Element path = (Element) svg.getElementsByTagName("path").item(0);
            shape = parsePath(path.getAttribute("d"));
            color = Color.decode(path.getAttribute("fill"));
        } catch (Exception e) {
            throw new IllegalStateException("별 아이콘을 읽을 수 없습니다: " + filename, e);
        }
    }

    private static InputStream open(String filename) throws Exception {
        InputStream bundled = StarSvgIcon.class.getResourceAsStream("/resources/icon/" + filename);
        if (bundled == null) throw new IOException("클래스패스 아이콘 리소스 누락: /resources/icon/" + filename);
        return bundled;
    }

    private static Path2D parsePath(String data) {
        var matcher = Pattern.compile("[A-Za-z]|[-+]?(?:[0-9]*\\.[0-9]+|[0-9]+)(?:[eE][-+]?[0-9]+)?").matcher(data);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) tokens.add(matcher.group());
        Path2D path = new Path2D.Double();
        int i = 0;
        while (i < tokens.size()) {
            String command = tokens.get(i++);
            switch (command) {
                case "M" -> path.moveTo(Double.parseDouble(tokens.get(i++)), Double.parseDouble(tokens.get(i++)));
                case "C" -> path.curveTo(Double.parseDouble(tokens.get(i++)), Double.parseDouble(tokens.get(i++)),
                        Double.parseDouble(tokens.get(i++)), Double.parseDouble(tokens.get(i++)),
                        Double.parseDouble(tokens.get(i++)), Double.parseDouble(tokens.get(i++)));
                case "V" -> path.lineTo(path.getCurrentPoint().getX(), Double.parseDouble(tokens.get(i++)));
                case "Z" -> path.closePath();
                default -> throw new IllegalArgumentException("지원하지 않는 별 SVG 명령: " + command);
            }
        }
        return path;
    }

    /**
     * 아이콘 표시 너비를 반환함.
     *
     * @return 생성 시 지정한 너비
     */
    @Override public int getIconWidth() { return width; }
    /**
     * 아이콘 표시 높이를 반환함.
     *
     * @return 생성 시 지정한 높이
     */
    @Override public int getIconHeight() { return height; }

    /**
     * SVG 별 경로를 표시 크기로 배율 조정해 그림.
     *
     * @param c 아이콘을 표시할 컴포넌트; 그리기에는 사용하지 않음
     * @param g Graphics2D 호환 그래픽 컨텍스트
     * @param x 시작 x 좌표
     * @param y 시작 y 좌표
     */
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.translate(x, y);
        graphics.scale(width / viewWidth, height / viewHeight);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(color);
        graphics.fill(shape);
        graphics.dispose();
    }
}
