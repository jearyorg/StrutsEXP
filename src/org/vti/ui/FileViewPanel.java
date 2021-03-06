package org.vti.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.vti.enumeration.Version;
import org.vti.service.ExploitService;
import org.vti.service.impl.Struts2_S016_ExploitServiceImpl;
import org.vti.service.impl.Struts2_S019_ExploitServiceImpl;
import org.vti.service.impl.Struts2_S09_ExploitServiceImpl;

public class FileViewPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;
	
	private DefaultMutableTreeNode root=new DefaultMutableTreeNode("我的电脑");

	private JTree tree;
	
	private JTabbedPane fileContentJTabbedPane;
	
	private JTextPane fileContentJTextPane;
	
	private JPopupMenu rightJPopupMenu;
	
	private JFileChooser filesaveChooser;

	private String host;
	
	private Version version;
	
	public FileViewPanel(){
		setSize(600,460);
		setVisible(true);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(160);
		
		tree=new JTree(root);
		
		JScrollPane fileJtreeJScrollPane=new JScrollPane(tree);
		
		fileContentJTabbedPane=new JTabbedPane();
		
		this.fileContentJTabbedPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getClickCount() == 2)&&(FileViewPanel.this.fileContentJTabbedPane.getTabCount()> 0)) {
					FileViewPanel.this.fileContentJTabbedPane.remove(FileViewPanel.this.fileContentJTabbedPane.getSelectedIndex());
				}
			}
		});
		
		tree.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (tree.getSelectionCount() != 0 && e.getButton() == 1 && e.getClickCount() == 2) {
					
					if (tree.getSelectionPath().toString().equals("[我的电脑]")) {
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								getRoots();
							}
						}).start();
						
					}else {
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								
								String path = "";
								String longText = tree.getSelectionPath().toString();
								String[] text = longText.substring(1, longText.length() - 1).split(",");
								for (int k = 0; k < text.length; k++) {
									path = path + text[k].trim() + "/";
								}
								path = path.substring(4, path.length());
								String rquestPath = path.replaceAll("\\\\", "/");
								
								DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
								
								if(isDirectory(rquestPath)){
									List<String> fileNames= getFiles(rquestPath);
									
									selectedNode.removeAllChildren();
									
									if (fileNames!=null) {
										for (String fileName:fileNames) {
											selectedNode.add(new DefaultMutableTreeNode(fileName));
										}
										tree.repaint();
									}
									
								}else {
									String fileName = selectedNode.toString();
									String content = getFileContent(rquestPath);
									fileContentJTextPane = new JTextPane();
									fileContentJTextPane.setEditable(false);
									fileContentJTextPane.setText(content);
									fileContentJTextPane.setCaretPosition(0);
									fileContentJTabbedPane.addTab(fileName,fileContentJTextPane);
								}								
								
								
							}
						}).start();

					}
					
				}
				
				if (tree.getSelectionCount() != 0 && e.getButton() == 3) {
					rightJPopupMenu = new JPopupMenu();
					
					final JMenuItem download = new JMenuItem("下载");
					download.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (e.getSource()==download) {
								
								String path = "";
								String longText = tree.getSelectionPath().toString();
								String[] text = longText.substring(1, longText.length() - 1).split(",");
								for (int k = 0; k < text.length; k++) {
									path = path + text[k].trim() + "/";
								}
								path = path.substring(4, path.length());
								final String rquestPath = path.replaceAll("\\\\", "/");
								
								new Thread(new Runnable() {
									
									@Override
									public void run() {
										
										if(!isDirectory(rquestPath)){
											filesaveChooser = new JFileChooser();
											int option =filesaveChooser.showSaveDialog(null);
											
											if (option == JFileChooser.APPROVE_OPTION) {
												File file = filesaveChooser.getSelectedFile();
												try {
													
													FileOutputStream fos=new FileOutputStream(file);
													InputStream inputStream=getInputStream(rquestPath);
													
													int x=0;
													while ((x=inputStream.read())!=-1) {
														fos.write(x);
													}
													inputStream.close();
													
													fos.flush();
													fos.close();
													
													JOptionPane.showMessageDialog(null, "恭喜你，下载成功");
												} catch (Exception exp) {
													exp.printStackTrace();
													JOptionPane.showMessageDialog(null, "对不起,下载失败");
												}
											}
										}
									}
								}).start();
								
							}
						}
					});
					
					
					final JMenuItem refresh = new JMenuItem("刷新");
					refresh.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (e.getSource()==refresh) {
								
								new Thread(new Runnable() {
									
									@Override
									public void run() {
										
										String path = "";
										String longText = tree.getSelectionPath().toString();
										String[] text = longText.substring(1, longText.length() - 1).split(",");
										for (int k = 0; k < text.length; k++) {
											path = path + text[k].trim() + "/";
										}
										path = path.substring(4, path.length());
										String rquestPath = path.replaceAll("\\\\", "/");
										
										if(isDirectory(rquestPath)){
											DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
											
											if (selectedNode!=null) {
												List<String> fileNames= getFiles(rquestPath);
												selectedNode.removeAllChildren();
												
												for (String fileName:fileNames) {
													selectedNode.add(new DefaultMutableTreeNode(fileName));
												}
												
												SwingUtilities.invokeLater(new Runnable() {
													public void run() {
														tree.repaint();
														tree.updateUI();
													}
												});
											}
										}
										
									}
								}).start();
								
							}
						}
					});
					
					rightJPopupMenu.add(download);
					rightJPopupMenu.add(refresh);
					rightJPopupMenu.show(tree, e.getX(), e.getY());
				}
			}
			
		});
		
		JScrollPane fileContentJScrollPane=new JScrollPane(fileContentJTabbedPane);
		
	    splitPane.setLeftComponent(fileJtreeJScrollPane);
	    
	    splitPane.setRightComponent(fileContentJScrollPane);
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
		);
		
		setLayout(groupLayout);
	}
	
	
	public void setReqestUrl(String host){
		this.host=host;
	}
	
	public void setVersion(Version version){
		this.version=version;
	}
	
	private void getRoots(){
		try {
			root.removeAllChildren();
			if (host!=null) {
				
				ExploitService service=null;
				
				switch (version) {
				case S2009:
					service=new Struts2_S09_ExploitServiceImpl();
					break;
				case S2016:
					service=new Struts2_S016_ExploitServiceImpl();
					break;
				default:
					service=new Struts2_S019_ExploitServiceImpl();
					break;
				}
				
				List<String>diskList=service.doGetFileSystem(host);
				
				DefaultTreeModel defaultTreeModel = null;
				
				for (String disk:diskList) {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(disk);
					root.add(child);
				}
				defaultTreeModel = new DefaultTreeModel(this.root);
				tree.setModel(defaultTreeModel);
				tree.repaint();
				
			}else {
				JOptionPane.showMessageDialog(this, "请输入URL");
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			JOptionPane.showMessageDialog(this, "对不起,无法获取文件系统");
		}
	}
	
	
	private List<String> getFiles(String path){
		
		try {
			if (host!=null) {
				
				ExploitService service=null;
				
				switch (version) {
				case S2009:
					service=new Struts2_S09_ExploitServiceImpl();
					break;
				case S2016:
					service=new Struts2_S016_ExploitServiceImpl();
					break;
				default:
					service=new Struts2_S019_ExploitServiceImpl();
					break;
				}
				
				List<String>fileList=service.doListFiles(host, path);
				
				return fileList;
				
			}else {
				JOptionPane.showMessageDialog(this, "请输入URL");
				return null;
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			JOptionPane.showMessageDialog(this, "对不起,无法获取文件系统");
			return null;
		}
	}
	
	
	
	private boolean isDirectory(String path){
		try {
			if (host!=null) {
				
				ExploitService service=null;
				
				switch (version) {
				case S2009:
					service=new Struts2_S09_ExploitServiceImpl();
					break;
				case S2016:
					service=new Struts2_S016_ExploitServiceImpl();
					break;
				default:
					service=new Struts2_S019_ExploitServiceImpl();
					break;
				}
				
				boolean flag=service.doIsDirectory(host, path);

				return flag;
			}else {
				JOptionPane.showMessageDialog(this, "请输入URL");
				return false;
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			JOptionPane.showMessageDialog(this, "对不起,无法获取文件系统");
			return false;
		}
	}
	
	private String getFileContent(String path){
		try {
			if (host!=null) {
				
				ExploitService service=null;
				
				switch (version) {
				case S2009:
					service=new Struts2_S09_ExploitServiceImpl();
					break;
				case S2016:
					service=new Struts2_S016_ExploitServiceImpl();
					break;
				default:
					service=new Struts2_S019_ExploitServiceImpl();
					break;
				}
				
				return  service.doGetFileContent(host, path);
				
			}else {
				JOptionPane.showMessageDialog(this, "请输入URL");
				return  ""; 
				
			}
		} catch (Exception exp) {
			return exp.toString();
		}
	}
	
	private InputStream getInputStream(String path){
		try {
			if (host!=null) {
				
				ExploitService service=null;
				
				switch (version) {
				case S2009:
					service=new Struts2_S09_ExploitServiceImpl();
					break;
				case S2016:
					service=new Struts2_S016_ExploitServiceImpl();
					break;
				default:
					service=new Struts2_S019_ExploitServiceImpl();
					break;
				}
				
				return  service.doDownload(host, path);
				
			}else {
				JOptionPane.showMessageDialog(this, "请输入URL");
				return  null; 
				
			}
		} catch (Exception exp) {
			return null;
		}
	}
}
