package com.cjwsjy.gis.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.Binary;
import com.cjwsjy.gis.model.TerrainModel;
import com.cjwsjy.gis.model.TileModel;
import com.cjwsjy.gis.util.MongoDbAccess;
import com.cjwsjy.gis.util.SourceAccess;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TerrainTileService extends HttpServlet {

	private static final long serialVersionUID = 1785832824220356701L;
	
	private static final int MAX_ATTEMPTS = 100;
	private URLConnection connection = null;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		TileModel tileModel = new TileModel(request);
		
		MongoDatabase db = MongoDbAccess.getDatabase(tileModel.getDataSet());
		BasicDBObject obj = new BasicDBObject();
		String id = getMongoIdx(tileModel);
		obj.put("_id", id);
		
		MongoCursor<Document> iterator = db.getCollection("L_" + tileModel.getLevel()).find(obj).iterator();
		if (iterator.hasNext()) {
			Document doc = iterator.next();
			if (doc.getBoolean("failure")) {
				// MongoDB中已标识无法获取该瓦片
				response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
			} else {
				Binary binary = (Binary)doc.get("bytes");
				if (binary == null || binary.getData().length <= 0) {
					// 已尝试获取该瓦片但未成功，再次尝试
					ByteBuffer byteBuffer = getSource(tileModel);
					Document update = new Document("_id", id);
					if (byteBuffer == null) {
						int attempts = doc.getInteger("attempts") + 1;
						update.append("attempts", attempts).append("failure", attempts >= MAX_ATTEMPTS);
						db.getCollection("L_" + tileModel.getLevel()).updateOne(doc, new Document("$set", update));
					} else {
						byte[] b = byteBuffer.array();
						update.append("bytes", b);
						update.append("suffix", ".terrain");
						update.append("format", "quantized-mesh-1.0");
						db.getCollection("L_" + tileModel.getLevel()).updateOne(doc, new Document("$set", update));
						output(b, response);
					}
				} else {
					// MongoDB数据已存储
					byte[] b = binary.getData();
					output(b, response);
				}
			}
		} else {
			Document doc = new Document("_id", id).append("attempts", 1);
			doc.append("failure", false);
			doc.append("suffix", ".terrain");
			doc.append("format", "quantized-mesh-1.0");
			ByteBuffer byteBuffer = getSource(tileModel);
			if (byteBuffer != null) {
				byte[] b = byteBuffer.array();
				doc.append("bytes", b);
				output(b, response);
			} else {
				response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
			}
			db.getCollection("L_" + tileModel.getLevel()).insertOne(doc);
		}
		
	}
	
	private void output(byte[] b, HttpServletResponse response) throws IOException {
		response.addHeader("Content-Encoding", "gzip");
		response.setContentType("application/vnd.quantized-mesh;extensions=octvertexnormals-watermask");
		response.setHeader("Content-Length",String.valueOf(b.length));
		response.setHeader("Access-Control-Allow-Origin","*");
		response.setHeader("Vary","Accept");
		OutputStream output = null;
		try {
			output = response.getOutputStream();
			output.write(b, 0, b.length);
		} catch(Exception ex) {
			ex.printStackTrace();
			response.sendError(HttpURLConnection.HTTP_NO_CONTENT);
		} finally {
			output.close();
		}
	}
	
	private String getMongoIdx(TileModel tileModel) {
		return tileModel.getRow() + "_" + tileModel.getCol();
	}

	private ByteBuffer getSource(TileModel tile) {
		String urlStr = TerrainModel.getSourceURL(tile);
		ByteBuffer byteBuffer = null;
		try {
			connection = SourceAccess.openTerrainConnection(new URL(urlStr));
//			//connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
//			String extensions = "octvertexnormals-watermask";
//			String header = "application/vnd.quantized-mesh;extensions=" + extensions + ",application/octet-stream;q=0.9,*/*;q=0.01";
//			// connection.setRequestProperty("User-Agent","MSIE");
//			connection.setRequestProperty("Accept", header);
			byteBuffer = SourceAccess.read(connection);
		} catch (Exception e) {
			System.err.println("can not get tile: " + urlStr);
			System.err.println(e.getMessage());
			return null;
		}
		return byteBuffer;
	}
}
