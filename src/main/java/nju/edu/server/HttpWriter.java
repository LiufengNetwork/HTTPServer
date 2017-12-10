package nju.edu.server;

import nju.edu.HttpStatus;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static nju.edu.utils.HttpUtils.*;

public class HttpWriter {
    private HttpStatus status;
    private DataOutputStream outputStream;

    public HttpWriter(Socket socket) {
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    设置response的首部
 */
    public void setResponseHeader(HttpStatus httpStatus) throws IOException {
        status = httpStatus;
        outputStream.write(httpStatus.getInitialLineBytes());
        outputStream.flush();
    }

    /**
     * Writes out the "Last-modified" header entry to the response output stream, appending "\r\n".
     * <p>
     * The date is formatted using {@code DateTimeFormatter.RFC_1123_DATE_TIME}.
     *
     * @param instant the last-modified header entry value
     * @throws IOException indicating an error occurred while writing out to the output stream
     */
    public void writeLastModified(Instant instant) throws IOException {
        writeHeader(LAST_MODIFIED, instant == null ? "Never" :
                DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.of("GMT"))));
    }

    /**
     * Writes out a header entry to the response output stream, appending "\r\n".
     *
     * @param key   the header entry key
     * @param value the header entry value
     * @throws IOException indicating an error occurred while writing out to the output stream
     */
    public void writeHeader(String key, String value) throws IOException {
        writeHeader(key + ":" + value);
    }

    /**
     * Writes out a header entry to the response output stream, appending "\r\n".
     *
     * @param header the header entry
     * @throws IOException indicating an error occurred while writing out to the output stream
     */
    public void writeHeader(String header) throws IOException {
        outputStream.write((header + "\r\n").getBytes(ASCII));
    }

    /**
     * Writes out "\r\n" to the response output stream which is required to end the response headers before writing out the body content.
     *
     * @throws IOException indicating an error occurred while writing out to the output stream
     */
    public void endHeader() throws IOException {
        outputStream.write(CR);
        outputStream.write(LF);
    }

    public void writeBody(byte[] data) throws IOException {
        outputStream.write(data);
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void close() {

    }

    public void badRequest() throws IOException {
        setResponseHeader(HttpStatus.BAD_REQUEST);
        endHeader();
        outputStream.close();
    }

    public void intervalError() throws IOException {
        setResponseHeader(HttpStatus.INTERNAL_SERVER_ERROR);
        endHeader();
        outputStream.close();
    }

    public HttpStatus getStatus() {
        return status;
    }
}
