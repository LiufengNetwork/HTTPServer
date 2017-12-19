package nju.edu.utils;

import nju.edu.HttpMethod;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.Character.isWhitespace;
import static java.net.InetAddress.getByName;




public class HttpUtils {
    public static final String rootPath = System.getProperty("user.dir" + "/src/main/resource");
    public  static  final  int port=8080;

    public static final Charset ASCII = Charset.forName("US-ASCII");
    public static final Charset UTF_8 = Charset.forName("utf8");

    public static final String CHARSET = "charset=";
    // Colon ':'
    public static final byte COLON = 58;

    public static final byte CR = 13; // \r

    public static final byte LF = 10; // \n

    public static final int ABORT_PROCESSING = -1;

    public static final String USER_AGENT = "user-agent";

    public static final String ACCEPT = "Accept";

    public static final String ETAG = "ETag";

    public static final String ACCEPT_ENCODING = "accept-encoding";

    public static final String TRANSFER_ENCODING = "transfer-encoding";

    public static final String CONTENT_ENCODING = "content-encoding";

    public static final String CONTENT_TYPE = "content-type";

    public static final String CHUNKED = "chunked";

    public static final String CONNECTION = "connection";

    public static final String LOCATION = "location";

    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String IF_NONE_MATCH = "If-None-Match";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String X_FORWARDED_FOR = "x-forwarded-for";

    public static final String CONTENT_LENGTH = "content-length";

    public static final String CACHE_CONTROL = "Cache-Control";

    // space ' '
    public static final byte SP = 32;

    public static final String EXPECT = "expect";

    public static final String CONTINUE = "100-continue";

    /**
     * judge if current HTTP method has entity body
     * @param method
     * @return
     */
    public static boolean hasBody(HttpMethod method){
        switch (method){
            case GET:
                return false;
            case HEAD:
                return false;
            case POST:
                return true;
            case PUT:
                return true;
            default:
                return true;
        }
    }

    private static final byte[] ALPHAS = "0123456789ABCDEF".getBytes();

    public static int findEndOfString(String sb, int offset) {
        int result;
        for (result = sb.length(); result > offset; result--) {
            if (!isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }

    public static int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (!isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    public static int findWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    public static int findColonIndex(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (sb.charAt(result) == COLON) {
                break;
            }
        }
        return result;
    }

    public static int getChunkSize(String hex) throws ProtocolException {
        hex = hex.trim();
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                hex = hex.substring(0, i);
                break;
            }
        }
        try {
            return Integer.parseInt(hex, 16);
        } catch (Exception e) {
            throw new ProtocolException("Expect chunk size to be a number, get" + hex);
        }
    }

    // content-type => Content-Type
    public static String camelCase(String key) {
        StringBuilder sb = new StringBuilder(key.length());
        boolean upper = true;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (upper) {
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(Character.toLowerCase(c));
            }
            upper = c == '-';
        }
        return sb.toString();
    }

  public static String getPath(URI uri) {
        String path = encodeURI(uri.getRawPath());
        String query = uri.getRawQuery();
        if ("".equals(path))
            path = "/";
        if (query == null)
            return path;
        else
            return path + "?" + query;
    }

    public static String encodeURI(String url) {
        byte[] bytes = url.getBytes(UTF_8);
        DynamicBytes buffer = new DynamicBytes(bytes.length * 2);
        boolean e = true;
        for (byte b : bytes) {
            int c = b < 0 ? b + 256 : b;
            if (c < '!' || c > '~') {
                e = true;
            } else {
                switch (c) {
                    case '"':
                        // https://github.com/http-kit/http-kit/issues/70
//                    case '%':
                    case '<':
                    case '>':
                    case '\\':
                    case '^':
                    case '`':
                    case '{':
                    case '}':
                    case '|':
                        e = true;
                        break;
                    default:
                        e = false;
                }
            }
            if (e) {
                buffer.append((byte) '%');
                buffer.append(ALPHAS[c / 16]);
                buffer.append(ALPHAS[c % 16]);
            } else {
                buffer.append(b);
            }
        }
        return new String(buffer.get(), 0, buffer.length(), UTF_8);
    }

    public static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            if ("https".equals(uri.getScheme()))
                port = 443;
            else
                port = 80;
        }
        return port;
    }

    public static String getHost(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort();

        if (port != -1) {
            host += ":" + port;
        }
        return host;
    }

    public static String getProxyHost(URI uri){
        if (uri.getPort() == -1){
            return uri.getHost();
        }

        return uri.getHost() + ":" + uri.getPort();
    }

    public static InetSocketAddress getServerAddr(URI uri) throws UnknownHostException {
        InetAddress host = getByName(uri.getHost());
        return new InetSocketAddress(host, getPort(uri));
    }

    public static byte[] readContent(File f, int length) throws IOException {
        byte[] bytes = new byte[length];
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
            int offset = 0;
            while (offset < length) {
                offset += fs.read(bytes, offset, length - offset);
            }
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (Exception ignore) {
                }
            }
        }
        return bytes;
    }

    public static ByteBuffer readAll(File f) throws IOException {
        int length = (int) f.length();
        if (length >= 1024 * 1024 * 20) { // 20M
            FileInputStream fs = new FileInputStream(f);
            MappedByteBuffer buffer = fs.getChannel().map(MapMode.READ_ONLY, 0, length);
            fs.close();
            return buffer;
        } else {
            return ByteBuffer.wrap(readContent(f, length));
        }
    }

    public static DynamicBytes readAll(InputStream is) throws IOException {
        DynamicBytes bytes = new DynamicBytes(32768); // init 32k
        byte[] buffer = new byte[16384];
        int read;
        while ((read = is.read(buffer)) != -1) {
            bytes.append(buffer, read);
        }
        is.close();
        return bytes;
    }

    public static String getStringValue(Map<String, Object> headers, String key) {
        Object o = headers.get(key);
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }

    public static void printError(String msg, Throwable t) {
        String error = String.format("%s [%s] ERROR - %s", new Date(), Thread.currentThread()
                .getName(), msg);
        StringWriter str = new StringWriter();
        PrintWriter pw = new PrintWriter(str, false);
        pw.println(error);
        t.printStackTrace(pw);
        System.err.print(str.getBuffer().toString());
    }

    public static void splitAndAddHeader(String sb, Map<String, Object> headers) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;

        nameStart = findNonWhitespace(sb, 0);
        for (nameEnd = nameStart; nameEnd < length; nameEnd++) {
            char ch = sb.charAt(nameEnd);
            if (ch == ':' || Character.isWhitespace(ch)) {
                break;
            }
        }

        for (colonEnd = nameEnd; colonEnd < length; colonEnd++) {
            if (sb.charAt(colonEnd) == ':') {
                colonEnd++;
                break;
            }
        }

        valueStart = findNonWhitespace(sb, colonEnd);
        valueEnd = findEndOfString(sb, valueStart);

        String key = sb.substring(nameStart, nameEnd);
        if (valueStart > valueEnd) { // ignore
            // logger.warn("header error: " + sb);
        } else {
            String value = sb.substring(valueStart, valueEnd);
            key = key.toLowerCase();
            Object v = headers.get(key);
            if (v != null) {
                // https://github.com/http-kit/http-kit/issues/108
                value = v.toString() + "," + value;
            }
            headers.put(key, value);
        }
    }

    /*----------------charset--------------------*/

    public static Charset parseCharset(String type) {
        if (type != null) {
            try {
                type = type.toLowerCase();
                int i = type.indexOf(CHARSET);
                if (i != -1) {
                    String charset = type.substring(i + CHARSET.length()).trim();
                    return Charset.forName(charset);
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    // <?xml version='1.0' encoding='GBK'?>
    // <?xml version="1.0" encoding="UTF-8"?>
    static final Pattern ENCODING = Pattern.compile("encoding=('|\")([\\w|-]+)('|\")",
            Pattern.CASE_INSENSITIVE);

    private static Charset guess(String html, String patten) {
        int idx = html.indexOf(patten);
        if (idx != -1) {
            int start = idx + patten.length();
            int end = html.indexOf('"', start);
            if (end != -1) {
                try {
                    return Charset.forName(html.substring(start, end));
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    public static final String CL = "Content-Length";
}
