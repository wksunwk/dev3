package com.brower.jcef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;

public class Test extends JFrame {
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
	private Test(String startURL, boolean useOSR, boolean isTransparent) {
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
				arg0.executeJavaScript(js.toString(), null, 0);
			}
		});
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

	public static void main(String[] args) {
		// 添加DLL库
		Test.addLibraryDir(System.getProperty("user.dir") + "/jcefdll");
		String url = "file://" + System.getProperty("user.dir") + "/html/test7.HTML";
		new Test(url, OS.isLinux(), false);
	}
}
