package com.brower.jcef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

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

public class PageSaveTest extends JFrame {
	private static final long serialVersionUID = -5570653778104813836L;
	private final JTextField address_;
	private final CefApp cefApp_;
	private final CefClient client_;
	private final CefBrowser browser_;
	private final Component browerUI_;
	
	private boolean isFirst = true;
	
	/*
	 * https://blog.csdn.net/u012414590/article/details/52879616
	 */

	/*
	 * test9.HTML,获取html页面图片,保存至本地,调试成功版本;
	 */
	private PageSaveTest(String startURL, boolean useOSR, boolean isTransparent) {
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
				// 热力图数据只生成一次
				if (!isFirst) {
					return;
				}
				isFirst = false;
				StringBuffer js = new StringBuffer();
				js.append("var data = generateRandomData(200);");
				js.append("heatmapInstance.setData(data);");
				js.append("updateForceDirection(data);");
				js.append("fetchPic();");
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
				arg0.executeJavaScript(js.toString(), null, 0);
			}
		});
		
        //    Beside the normal handler instances, we're registering a MessageRouter
        //    as well. That gives us the opportunity to reply to JavaScript method
        //    calls (JavaScript binding). We're using the default configuration, so
        //    that the JavaScript binding methods "cefQuery" and "cefQueryCancel"
        //    are used.
		/*
		 * 
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
//					return true;
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

	public static void main(String[] args) {
		// 添加DLL库
		PageSaveTest.addLibraryDir(System.getProperty("user.dir") + "/jcefdll");
		String url = "file://" + System.getProperty("user.dir") + "/html/test9.HTML";
		new PageSaveTest(url, OS.isLinux(), false);
	}
}

