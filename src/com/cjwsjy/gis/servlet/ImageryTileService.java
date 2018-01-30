package com.cjwsjy.gis.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.Binary;
import com.cjwsjy.gis.model.ImageryModel;
import com.cjwsjy.gis.model.TileModel;
import com.cjwsjy.gis.util.MongoDbAccess;
import com.cjwsjy.gis.util.SourceAccess;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class ImageryTileService extends HttpServlet {
	
	private static final long serialVersionUID = 6469502999216066844L;
	private static final int MAX_ATTEMPTS = 10000;
	private URLConnection connection = null;
	private static final int EMPTY_IMG_LENGTH_TIANDITU = 5615;
	private static final int EMPTY_IMG_LENGTH_BING = 1033;
	private static byte[] EMPTY_BYTES_TIANDITU = null;
	private static byte[] EMPTY_BYTES_BING = null;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		TileModel tile = new TileModel(request);
		
		MongoDatabase db = MongoDbAccess.getDatabase(tile.getDataSet());
		BasicDBObject obj = new BasicDBObject();
		String id = getMongoIdx(tile, request.getParameter("C"), request.getParameter("D"));
		obj.put("_id", id);
		MongoCursor<Document> iterator = db.getCollection("L_" + tile.getLevel()).find(obj).iterator();
		if (iterator.hasNext()) {
			Document doc = iterator.next();
			if (doc.getBoolean("failure")) {
				// MongoDB中已标识无法获取该瓦片
				response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
			} else {
				Binary binary = (Binary)doc.get("bytes");
				if (binary == null || binary.getData().length <= 0) {
					// 已尝试获取该瓦片但未成功，再次尝试
					ByteBuffer byteBuffer = getSource(tile);
					Document update = new Document("_id", id);
					if (byteBuffer == null) {
						int attempts = doc.getInteger("attempts") + 1;
						update.append("attempts", attempts).append("failure", attempts >= MAX_ATTEMPTS);
						db.getCollection("L_" + tile.getLevel()).updateOne(doc, new Document("$set", update));
					} else {
						byte[] b = byteBuffer.array();
						if (isTileEmpty(tile, b)) {
							update.append("failure", true);
							db.getCollection("L_" + tile.getLevel()).updateOne(doc, new Document("$set", update));
							response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
						} else {
							String contentType = ImageryModel.getContentType(tile);
							update.append("bytes", b);
							update.append("contentType", contentType);
							db.getCollection("L_" + tile.getLevel()).updateOne(doc, new Document("$set", update));
							output(id, contentType, b, response);
						}
					}
				} else {
					// MongoDB数据已存储
					byte[] b = binary.getData();
					String contentType = doc.getString("contentType");
					output(id, contentType, b, response);
				}
			}
		} else {
			Document doc = new Document("_id", id).append("attempts", 1);
			ByteBuffer byteBuffer = getSource(tile);
			if (byteBuffer != null) {
				byte[] b = byteBuffer.array();
				if (isTileEmpty(tile, b)) {
					doc.append("failure", true);
					db.getCollection("L_" + tile.getLevel()).insertOne(doc);
					response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
				} else {
					String contentType = ImageryModel.getContentType(tile);
					doc.append("bytes", b);
					doc.append("failure", false);
					doc.append("contentType", contentType);
					db.getCollection("L_" + tile.getLevel()).insertOne(doc);
					output(id, contentType, b, response);
				}
			} else {
				doc.append("failure", false);
				db.getCollection("L_" + tile.getLevel()).insertOne(doc);
				response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
			}
		}
	}
	
	private void output(String id, String contentType, byte[] b, HttpServletResponse response) throws IOException {
		if (contentType != null) {
			String suffix = contentType.replace("image/", ".");
			String fileName = new String((id + suffix).getBytes(),"ISO8859-1");
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
			response.setContentType(contentType);
		}
		OutputStream output = null;
		try {
			response.setHeader("Content-Length",String.valueOf(b.length));
			response.setHeader("Access-Control-Allow-Origin","*");
			output = response.getOutputStream();
			output.write(b, 0, b.length);
		} catch(Exception ex) {
			ex.printStackTrace();
			response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
		} finally {
			output.close();
		}
	}
	
	private String getMongoIdx(TileModel tileModel, String convert, String delta) {
		String idx = "";
		if (convert != null && Boolean.parseBoolean(convert)) {
			idx = tileModel.getRow() + "_" + tileModel.getCol();
		} else {
			double d  = delta == null ? 90 : Double.parseDouble(delta);
			int level = tileModel.getLevel();
			double currentDelta = d / Math.pow(2, level);
			int rowCount = (int) (180 / currentDelta);
			int originRow = tileModel.getRow();
			int col = tileModel.getCol();
			int row = Math.abs(originRow - (rowCount - 1));
			idx = row + "_" + col;
		}
		return idx;
	}
	
	private ByteBuffer getSource(TileModel tile) {
		String urlStr = ImageryModel.getSourceURL(tile);
		ByteBuffer byteBuffer = null;
		try {
			connection = SourceAccess.openConnection(new URL(urlStr));
			byteBuffer = SourceAccess.read(connection);
		} catch (Exception e) {
			System.err.println("can not get tile: " + urlStr);
			System.err.println(e.getMessage());
			return null;
		}
		return byteBuffer;
	}
	
	private boolean isTileEmpty(TileModel tile, byte[] b) {
		String dataset = tile.getDataSet().toLowerCase();
        return (dataset.indexOf("tian") >= 0 && b.length == EMPTY_IMG_LENGTH_TIANDITU && Arrays.equals(b, getEmptyByte(dataset)))
                || (dataset.indexOf("bing") >= 0 && b.length == EMPTY_IMG_LENGTH_BING && Arrays.equals(b, getEmptyByte(dataset)));
    }
	
	private byte[] getEmptyByte(String dataset) {
		if (dataset.indexOf("tianditu") >= 0) {
			if (EMPTY_BYTES_TIANDITU == null) {
				File emptyFile = new File(getServletContext().getRealPath("resources/Tian_empty.jpeg"));
				FileInputStream fis;
				EMPTY_BYTES_TIANDITU = new byte[EMPTY_IMG_LENGTH_TIANDITU];
				try {
					fis = new FileInputStream(emptyFile);
					fis.read(EMPTY_BYTES_TIANDITU);
					fis.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return EMPTY_BYTES_TIANDITU;
		} else if (dataset.indexOf("bing") >= 0) { 
			if (EMPTY_BYTES_BING == null) {
				File emptyFile = new File(getServletContext().getRealPath("resources/Bing_empty.jpeg"));
				FileInputStream fis;
				EMPTY_BYTES_BING = new byte[EMPTY_IMG_LENGTH_BING];
				try {
					fis = new FileInputStream(emptyFile);
					fis.read(EMPTY_BYTES_BING);
					fis.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return EMPTY_BYTES_BING;
		}
		return null;
	}

}
