package com.brower.jcef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.browser.CefMessageRouter.CefMessageRouterConfig;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class PageSave extends JFrame {
	private static final long serialVersionUID = -5570653778104813836L;
	private final JTextField address_;
	private final CefApp cefApp_;
	private final CefClient client_;
	private final CefBrowser browser_;
	private final Component browerUI_;
	
	/*
	 * http://blog.csdn.net/rongchaoliu/article/details/47830799
	 * https://www.patrick-wied.at/static/heatmapjs/example-legend-tooltip.html
	 */

	/*
	 * test7.HTML,调试成功版本;
	 * 从java中传入应力数据即可;
	 * 
	 * 另,
	 * test4.HTML是配色示例;
	 * test5.HTML是没有增加应力方向的版本;
	 * test6.HTML与test7.HTML的区别在于数据来自于页面;
	 */
	private PageSave(String startURL, boolean useOSR, boolean isTransparent) {
		CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
			@Override
			public void stateHasChanged(org.cef.CefApp.CefAppState state) {
				// Shutdown the app if the native CEF part is terminated
				if (state == CefAppState.TERMINATED)
					System.exit(0);
			}
		});
		CefSettings settings = new CefSettings();
		settings.windowless_rendering_enabled = useOSR;
		cefApp_ = CefApp.getInstance(settings);
		client_ = cefApp_.createClient();
		client_.addLoadHandler(new CefLoadHandlerAdapter() {
			@Override
			public void onLoadEnd(CefBrowser arg0, CefFrame arg1, int arg2) {
				// TODO Auto-generated method stub
				super.onLoadEnd(arg0, arg1, arg2);
				StringBuffer js = new StringBuffer();
//				js.append("var div = document.getElementById('heatmap');");
//				js.append("var heatmapInstance = h337.create({container: div});");
//				js.append("var points = [];");
//				js.append("var max = 0;");
//				js.append("var width = 600;");
//				js.append("var height = 400;");
//				js.append("var len = 200;");
//				js.append("while (len--) {");
//				js.append("var val = Math.floor(Math.random()*100);");
//				js.append("max = Math.max(max, val);");
//				js.append("var point = {");
//				js.append("x: Math.floor(Math.random()*width),");
//				js.append("y: Math.floor(Math.random()*height),");
//				js.append("value: val");
//				js.append("};");
//				js.append("points.push(point);");
//				js.append("}");
//				js.append("var data = {");
//				js.append("max: max,");
//				js.append("data: points");
//				js.append("};");
//				js.append("heatmapInstance.setData(data);");
				js.append("var data = generateRandomData(200);");
				js.append("heatmapInstance.setData(data);");
				js.append("updateForceDirection(data);");
				/*
				 * 
                   Execute a string of JavaScript code in this frame. 
                   The url parameter is the URL where the script in question can be found, if any. 
                   The renderer may request this URL to show the developer the source of the error. 
                   The line parameter is the base line number to use for error reporting.
                   
                   void executeJavaScript(String code, String url, int line);
				   		code - The code to be executed.
				   		url - The URL where the script in question can be found.
				   		line - The base line number to use for error reporting.
				 */
//				arg0.executeJavaScript(js.toString(), null, 0);
			}
		});
		
        //    Beside the normal handler instances, we're registering a MessageRouter
        //    as well. That gives us the opportunity to reply to JavaScript method
        //    calls (JavaScript binding). We're using the default configuration, so
        //    that the JavaScript binding methods "cefQuery" and "cefQueryCancel"
        //    are used.
		/*
		 * https://blog.csdn.net/u012414590/article/details/52879616
		 */
		// Example config object showing the default values.
		CefMessageRouterConfig config = new CefMessageRouterConfig();
		config.jsQueryFunction = "cefQuery";
		config.jsCancelFunction = "cefQueryCancel";
		CefMessageRouter msgRouter = CefMessageRouter.create(config);
		msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
			@Override
			public boolean onQuery(CefBrowser browser, CefFrame frame, long query_id, String request,
					boolean persistent, CefQueryCallback callback) {
				if (request.indexOf("data:image/png;base64,") == 0) {
					// Reverse the message and return it to the JavaScript
					// caller.
					generateImage(request.substring("data:image/png;base64,".length()));
//					callback.success(new StringBuilder(msg).reverse().toString());
					return true;
				}
				// Not handled.
				return false;
			}
		}, true);
		// msgRouter.addHandler(new MessageRouterHandlerEx(client_), false);
		client_.addMessageRouter(msgRouter);
		
		browser_ = client_.createBrowser(startURL, useOSR, isTransparent);
		browerUI_ = browser_.getUIComponent();
		address_ = new JTextField(startURL, 100);
		address_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browser_.loadURL(address_.getText());
			}
		});
		getContentPane().add(address_, BorderLayout.NORTH);
		getContentPane().add(browerUI_, BorderLayout.CENTER);
		pack();
		setSize(1200, 800);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				CefApp.getInstance().dispose();
				dispose();
			}
		});
	}

	/**
	 * 添加DLL库路径
	 * 
	 * @param libraryPath
	 */
	public static void addLibraryDir(String libraryPath) {
		try {
			Field userPathsField = ClassLoader.class.getDeclaredField("usr_paths");
			userPathsField.setAccessible(true);
			String[] paths = (String[]) userPathsField.get(null);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < paths.length; i++) {
				if (libraryPath.equals(paths[i])) {
					continue;
				}
				sb.append(paths[i]).append(';');
			}
			sb.append(libraryPath);
			System.setProperty("java.library.path", sb.toString());
			final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 图片转化成base64字符串
	public static String getImageStr() {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		String imgFile = "D:\\tupian\\a.jpg";// 待处理的图片
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data);// 返回Base64编码过的字节数组字符串
	}

	// base64字符串转化成图片
	public static boolean generateImage(String imgStr) { // 对字节数组字符串进行Base64解码并生成图片
		if (imgStr == null) // 图像数据为空
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			// 生成jpeg图片
			String imgFilePath = "D:\\test\\saveScreen4.png";// 新生成的图片
			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static void savePic(JFrame jf){  
	    //得到窗口内容面板  
	    Container content=jf.getContentPane();  
	    //创建缓冲图片对象  
	    BufferedImage img=new BufferedImage(  
	            jf.getWidth(),jf.getHeight(),BufferedImage.TYPE_INT_RGB);  
	    //得到图形对象  
	    Graphics2D g2d = img.createGraphics();  
	    //将窗口内容面板输出到图形对象中  
	    content.printAll(g2d);  
	    //保存为图片  
	    File f=new File("D:\\test\\saveScreen.jpg");  
	    try {  
	        ImageIO.write(img, "jpg", f);  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	    //释放图形对象  
	    g2d.dispose();  
	}
	
	public static void savePic2(JFrame frame) {
		BufferedImage bi = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		frame.paint(g2d);
		try {
			ImageIO.write(bi, "PNG", new File("D:\\test\\saveScreen2.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void savePic3(JFrame frame, CefBrowser brower) {
		BufferedImage bi = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		brower.getUIComponent().getParent().paintAll(g2d);
		try {
			ImageIO.write(bi, "PNG", new File("D:\\test\\saveScreen3.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Component getbrowerUI() {
		return browerUI_;
	}
	
	public CefBrowser getBrower() {
		return browser_;
	}

	public static void main(String[] args) {
		// 添加DLL库
		PageSave.addLibraryDir(System.getProperty("user.dir") + "/jcefdll");
		String url = "file://" + System.getProperty("user.dir") + "/html/test8.HTML";
		new PageSave(url, OS.isLinux(), false);
	}
}

