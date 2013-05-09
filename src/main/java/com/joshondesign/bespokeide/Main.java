/*
 * Copyright 2013 Adam L. Davis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshondesign.bespokeide;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Types;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;

/**
 * Created by IntelliJ IDEA. User: josh Date: 6/20/12 Time: 2:53 PM To change
 * this template use File | Settings | File Templates.
 */
public class Main {
	private final int anchor = GridBagConstraints.CENTER;
	private final JButton openButton = new JButton("Open");
	private final JButton compileButton = new JButton("Compile");
	private final JTextArea errorConsole = new JTextArea("console");
	private final GridBagLayout mainLayout = new GridBagLayout();
	private final JPanel mainPanel = new JPanel(mainLayout);
	{
		// <xy x="20" y="20" width="549" height="469"/>
		// <preferredSize width="1000" height="700"/>
		mainPanel.setLocation(20, 20);
		mainPanel.setSize(549, 469);
		mainPanel.setPreferredSize(new Dimension(1000, 700));
	}
	private final JList<String> packageList = new JList<>();
	private final JList<String> classList = new JList<>();
	private final JList<MethodModel> methodList = new JList<>();
	private final JProgressBar progBar = new JProgressBar();
	private final JToolBar toolBar = new JToolBar();
	{
		toolBar.add(openButton);
		toolBar.add(compileButton);
		toolBar.add(progBar);
		mainPanel.add(toolBar);
		mainLayout.setConstraints(toolBar, new GridBagConstraints(0, 0, 4, 1,
				1.0, 0.0, GridBagConstraints.NORTH, 1, new Insets(0, 0, 0, 0),
				6, 0));
		toolBar.setPreferredSize(new Dimension(-1, 20));
	}
	private final JLabel locationLabel = new JLabel("Label");
	private final JTextArea methodBody = new JTextArea();
	{
		methodBody.setEditable(true);
		methodBody.setFont(Font.decode(Font.MONOSPACED).deriveFont(11f));
	}
	private final JTextArea notesArea = new JTextArea("Notes");
	{
		notesArea.setColumns(20);
		notesArea.setRows(10);
		notesArea.setEditable(true);
		notesArea.setFont(Font.decode(Font.MONOSPACED).deriveFont(11f));
	}
	private final JSplitPane mainSplitPane = new JSplitPane();
	{
		mainSplitPane.setDividerLocation(136);
		mainSplitPane.setContinuousLayout(true);
		mainPanel.add(mainSplitPane);
		mainLayout.setConstraints(mainSplitPane, new GridBagConstraints(0, 1,
				1, 1, 1.0, 1.0, anchor, GridBagConstraints.BOTH, new Insets(0,
						0, 0, 0), 0, 0));
		mainSplitPane.setPreferredSize(new Dimension(200, 200));
	}
	private final GridBagLayout mainLeftLayout = new GridBagLayout();
	private final JPanel mainLeftPane = new JPanel(mainLeftLayout);
	{
		mainSplitPane.setLeftComponent(mainLeftPane);
		final JScrollPane packagePane = new JScrollPane(packageList);
		mainLeftLayout.setConstraints(packagePane, new GridBagConstraints(0, 0,
				1, 1, 1.0, 1.0, anchor, 3, new Insets(0, 0, 0, 0), 7, 7));
		mainLeftPane.add(packagePane);

		final JScrollPane classPane = new JScrollPane(classList);
		mainLeftLayout.setConstraints(classPane, new GridBagConstraints(1, 0,
				1, 1, 1.0, 1.0, anchor, 3, new Insets(0, 0, 0, 0), 7, 7));
		mainLeftPane.add(classPane);

		final JScrollPane methodPane = new JScrollPane(methodList);
		mainLeftLayout.setConstraints(methodPane, new GridBagConstraints(2, 0,
				1, 1, 1.0, 1.0, anchor, 3, new Insets(0, 0, 0, 0), 7, 7));
		mainLeftPane.add(methodPane);
	}
	private final JSplitPane mainRightPane = new JSplitPane();
	{
		mainSplitPane.setRightComponent(mainRightPane);
		mainRightPane.setDividerLocation(275);
		mainRightPane.setOrientation(0);
		mainRightPane.setResizeWeight(1.0);
		mainRightPane.setRightComponent(new JScrollPane(errorConsole));
	}
	private final GridBagLayout mainRightTopLayout = new GridBagLayout();
	private final JPanel mainRightTopPanel = new JPanel(mainRightTopLayout);
	{
		mainRightPane.setLeftComponent(mainRightTopPanel);
		mainRightTopPanel.add(locationLabel);
		mainRightTopLayout.setConstraints(locationLabel,
				new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, anchor,
						GridBagConstraints.HORIZONTAL, 
						new Insets(0, 0, 0, 0), 0, 0));
	}
	private final JSplitPane notesAndMethodPane = new JSplitPane();
	{
		mainRightTopPanel.add(notesAndMethodPane);
		mainRightTopLayout.setConstraints(notesAndMethodPane,
				new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, anchor, 
						GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 3, 3));
		notesAndMethodPane.setDividerLocation(400);
		notesAndMethodPane.setResizeWeight(1.0);
		notesAndMethodPane.setRightComponent(new JScrollPane(notesArea));
		notesAndMethodPane.setLeftComponent(new JScrollPane(methodBody));
	}

	private ClassModel currentClass;
	private PackageModel currentPackage;
	public CodebaseModel packages;
	private JavacTask task;
	private Trees trees;
	private MethodModel currentMethod;
	private Iterable<? extends CompilationUnitTree> ASTs;
	private ConsoleHandler consoleHandler;
	private ProcessingEnvironment env;
	private Types types;

	@SuppressWarnings("serial")
	public Main() {

		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				progBar.setIndeterminate(true);
				loadCodebase();
			}
		});

		packageList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				String name = (String) packageList.getSelectedValue();
				currentPackage = packages.packageMap.get(name);
				classList.setModel(new AbstractListModel<String>() {
					public int getSize() {
						if (currentPackage == null || currentPackage.classList == null) {
							return 0;
						}
						return currentPackage.classList.size();
					}

					public String getElementAt(int i) {
						return currentPackage.classList.get(i).name;
					}
				});
				classList.clearSelection();
				methodList.clearSelection();
				methodList.setModel(new AbstractListModel<MethodModel>() {
					public int getSize() {
						return 0;
					}

					public MethodModel getElementAt(int i) {
						return null;
					}
				});
			}
		});

		classList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				String name = (String) classList.getSelectedValue();
				if (currentPackage == null || currentPackage.classMap == null) {
					return;
				}
				currentClass = currentPackage.classMap.get(name);
				methodList.setModel(new AbstractListModel<MethodModel>() {
					public int getSize() {
						if (currentClass == null)
							return 0;
						return currentClass.getMethodCount();
					}

					public MethodModel getElementAt(int i) {
						return currentClass.getMethod(i);
					}
				});
			}
		});

		methodList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				if (methodList.getSelectedIndex() < 0)
					return;
				currentMethod = (MethodModel) methodList.getSelectedValue();
				methodBody.setText(currentMethod.body.toString());
			}
		});

		methodList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel comp = (JLabel) super.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);
				if (comp != null && value != null
						&& value instanceof MethodModel) {
					MethodModel meth = (MethodModel) value;
					comp.setText(meth.getUniqueName());
				}
				return comp;
			}
		});

		compileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					compileCode();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		methodBody.addCaretListener(new CaretMoveHandler());
	}

	private void loadCodebase() {
		new Thread(new Runnable() {
			public void run() {
				try {
					realLoadCodebase();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}

	private void realLoadCodebase() throws IOException {
		consoleHandler = new ConsoleHandler(errorConsole);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				consoleHandler, null, null);
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
				Arrays.asList(Util.getTempDir()));

		// setup source path
		final JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Open One or More Java Source Directories");
		chooser.setCurrentDirectory(Util.getUserHome());
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.showOpenDialog(mainPanel);
		// File pth = new
		// File("/Users/josh/projects/Leo/LeonardoSketch/Sketch/src/");
		if (chooser.getSelectedFiles().length == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progBar.setIndeterminate(false);
				}
			});
			return;
		}

		fileManager.setLocation(StandardLocation.SOURCE_PATH,
				Arrays.asList(chooser.getSelectedFiles()));

		// get all source files
		Set<JavaFileObject.Kind> kinds = new HashSet<JavaFileObject.Kind>();
		kinds.add(JavaFileObject.Kind.SOURCE);
		Iterable<JavaFileObject> files = fileManager.list(
				StandardLocation.SOURCE_PATH, "", kinds, true);

		task = (JavacTask) compiler.getTask(null, fileManager, consoleHandler,
				null, null, files);

		FullCodeProcessor proc2 = new FullCodeProcessor();
		task.setProcessors(Arrays.asList(proc2));

		// task.call();
		task.setTaskListener(consoleHandler);
		// break the call() into individual pieces
		ASTs = task.parse(); // just parse the code
		task.analyze(); // fill in the types
		trees = Trees.instance(task);
		packages = proc2.getPackages(); // get the code structure back
		env = proc2.getEnv();
		types = proc2.types;

		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("serial")
			public void run() {
				packageList.setModel(new AbstractListModel<String>() {

					public int getSize() {
						return packages.packageList.size();
					}

					public String getElementAt(int i) {
						return packages.packageList.get(i);
					}
				});
				progBar.setIndeterminate(false);
			}
		});
	}

	// private JavacTask setupCompiler() throws IOException {
	// return (JavacTask) task;
	// }

	private void compileCode() throws IOException {
		task.generate();
	}

	public static void p(String s) {
		System.out.println(s);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Main");
		frame.setContentPane(new Main().mainPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private class CaretMoveHandler implements CaretListener {
		public void caretUpdate(CaretEvent caretEvent) {
			if (currentMethod == null)
				return;
			int dot = caretEvent.getDot(); // cursor position in characters

			CurrentMethodScanner scanner = new CurrentMethodScanner(dot,
					currentMethod, trees, Main.this, env);
			// scan every AST
			for (CompilationUnitTree ast : ASTs) {
				scanner.scan(ast, (Void) null);
			}
			locationLabel.setText(scanner.getDeepestPath());
			if (scanner.getInvokedMethod() != null) {
				notesArea.setText(scanner.getInvokedMethod().body.toString());
			} else {
				notesArea.setText("");
			}
		}
	}
}
