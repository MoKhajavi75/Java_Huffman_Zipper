package huffman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class huffman {
	
	//comparator 
	//this will sort the data related to the H_Node with this implementation
	private static class HuffmanComparator implements Comparator<H_Node>
	{
		@Override
		public int compare(H_Node node1,H_Node node2)
		{
			return node1.freq - node2.freq;
		}
	}
	
	//Default Constructor
	public huffman()
	{};
	
	//Compression is called from main
	public static void compress(String input) throws FileNotFoundException,IOException
	{
		if(input == null)
		{
			throw new NullPointerException("Input cannot be null.");
		}
		if(input.length() == 0)
		{
			throw new IllegalArgumentException("Encoding done for UTF-16");
			
		}
		String Put_Path = null;
		String File_name = null;
		//select the path to put the compressed file
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("cmp",".cmp","CMP");
		chooser.setFileFilter(filter);
		int retrival = chooser.showSaveDialog(null);
	    if (retrival == JFileChooser.APPROVE_OPTION) {
	    	System.out.println(chooser.getCurrentDirectory().toString());
	    	Put_Path = chooser.getCurrentDirectory().toString();
	    	File_name = chooser.getSelectedFile().getName();
	    	System.out.println(File_name);
	    } else {
	        System.out.println("No Selection ");
	    }
		
		//this will make a key for each character
		final Map<Character,Integer> charFreq = getCharFrequency(input);
		//this will make a huffman tree with given character frequencies
		final H_Node root = buildTree(charFreq);
		final Map<Character,String> charCode = CodeGenerator(charFreq.keySet(),root);
		final String encoded_input = encodeInput(charCode,input);
		serializeTree(root,Put_Path,File_name);
		serializeInput(encoded_input,Put_Path,File_name);	
	}
	
	/*This function will find each character frequency*/
	private static Map<Character,Integer> getCharFrequency(String input)
	{
		//return value declared here
		final Map<Character,Integer> map = new HashMap<Character,Integer>();
		//putting the values(Frequencies) in HashMap
		
		for(int i=0;i<input.length();++i)
		{
			char ch = input.charAt(i);
			/*boolean containsKey(Object key): It is a boolean 
			function which returns true or false based on whether the specified
			key is found in the map.*/
			if(map.containsKey(ch))
			{
				//this will find the character in the sentence and puts the # of repeats in the map
				int val = map.get(ch);
				map.put(ch, ++val);
			}
			else
			{
				map.put(ch, 1);
			}	
		}
		return map;
	}
	
	/*This function will build the tree*/
	private static H_Node buildTree(Map<Character,Integer> map)
	{
		//This will make a priority queue with the given map
		final Queue<H_Node> Priority_Queue = createPrioQueue(map);
		while(Priority_Queue.size()>1)
		{
			final H_Node node1 = Priority_Queue.remove();
			final H_Node node2 = Priority_Queue.remove();
			//merge removed nodes then put it back to the queue
			H_Node node = new H_Node('\0',node1.freq + node2.freq,node1,node2);
			Priority_Queue.add(node);
		}
		
		//prevent object leak
		return Priority_Queue.remove();
	}
	
	//return object will be a Queue of H_Nodes 
	private static Queue<H_Node> createPrioQueue(Map<Character,Integer> map)
	{
		/*
			Just pass appropriate Comparator to the constructor:
			PriorityQueue(int initialCapacity, Comparator<? super E> comparator)
			The only difference between offer and add is the interface they belong to. offer belongs to Queue<E>, whereas add is originally seen in Collection<E> interface. Apart from that both methods do exactly the same thing - insert the specified element into priority queue.
		 */
		final Queue<H_Node> P_Q = new PriorityQueue<H_Node>(10,new HuffmanComparator());
		
		for(Entry<Character, Integer> entry : map.entrySet())
		{
			P_Q.add(new H_Node(entry.getKey(),entry.getValue(),null,null));
		}
		return P_Q;
	}
	
	//Set keySet(): It returns the Set of the keys fetched from the map
	private static Map<Character,String> CodeGenerator(Set<Character> chars,H_Node node)
	{
		final Map<Character,String> map = new HashMap<Character,String>();
		Generator(node,map,"");
		return map;
	}
	//recursive function finding each character code 
	//example: A -> 0110 , B -> 001
	private static void Generator(H_Node node,Map<Character,String> map,String s)
	{
		if (node.left == null && node.right == null) {
            map.put(node.ch, s);
            return;
        }    
        Generator(node.left, map, s + '0');
        Generator(node.right, map, s + '1' );
	}
	
	private static String encodeInput(Map<Character,String> charCode,String input)
	{
		final StringBuilder S_B = new StringBuilder();
		for(int i=0;i<input.length();i++)
		{
			S_B.append(charCode.get(input.charAt(i)));
		}
		return S_B.toString();
	}
	
	private static void serializeTree(H_Node node,String Put_Path,String File_name) throws FileNotFoundException,IOException
	{
		final BitSet bitset = new BitSet();
		try(ObjectOutputStream oos_Tree = new ObjectOutputStream(new FileOutputStream(Put_Path+"/"+"tree")))
		{
			try(ObjectOutputStream oos_Char = new ObjectOutputStream(new FileOutputStream(Put_Path+"/"+"char")))
			{
				IntObject o = new IntObject();
				preOrder(node,oos_Char,bitset,o);
				// padded to mark end of bit set relevant for deserialization.
				bitset.set(o.bitPosition,true);
				oos_Tree.writeObject(bitset);
			}
		}	
	}
	private static class IntObject 
	{
		int bitPosition;
	}
	
	//preorder traversal,registering the tree in the bitset
	private static void preOrder(H_Node node,ObjectOutputStream oosChar,BitSet bitset,IntObject intobject) throws IOException
	{
		if (node.left == null && node.right == null) {
            bitset.set(intobject.bitPosition++, false);  // register branch in bitset
            oosChar.writeChar(node.ch);
            return;                                  // DONT take the branch.
        }
        bitset.set(intobject.bitPosition++, true);           // register branch in bitset
        preOrder(node.left, oosChar, bitset, intobject); // take the branch.

        bitset.set(intobject.bitPosition++, true);               // register branch in bitset
        preOrder(node.right, oosChar, bitset, intobject);    // take the branch.
    }
	/*Serialization is to store tree in a file so that 
	 * it can be later restored. The structure of tree must be maintained.
	 * Deserialization is reading tree back from file.
	 */
    private static void serializeInput(String message,String Put_path,String File_name) throws IOException 
    {
        final BitSet bitset = getBitSet(message);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Put_path+"/" + File_name+".cmp"))){
            oos.writeObject(bitset);
        } 
    }
    private static BitSet getBitSet(String message)
    {
    	final BitSet bitset = new BitSet();
    	int i=0;
    	for(i=0;i<message.length();i++)
    	{
    		if(message.charAt(i)=='1')
    		{
    			bitset.set(i,true);
    		}
    		else
    		{
    			bitset.set(i,false);
    		}
    	}
    	//this will show the end
    	bitset.set(i,true);
    	return bitset;
    }
    
    public static void expand(String File_dir,String File_name) throws FileNotFoundException,ClassNotFoundException,IOException
    {
    	final H_Node root = deserializeTree(File_dir);
    	decodeMessage(root,File_dir+"/"+File_name);
    }
    private static H_Node deserializeTree(String File_url) throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream oisBranch = new ObjectInputStream(new FileInputStream(File_url+"/"+"tree"))) {
            try (ObjectInputStream oisChar = new ObjectInputStream(new FileInputStream(File_url+"/"+"char"))) {
                final BitSet bitSet = (BitSet) oisBranch.readObject();
                return preOrder(bitSet, oisChar, new IntObject());
            }
        }
    }
    private static H_Node preOrder(BitSet bitSet, ObjectInputStream oisChar, IntObject o) throws IOException {   
        // created the node before reading whats registered.
        final H_Node node = new H_Node('\0', 0, null, null);

        // reading whats registered and determining if created node is the leaf or non-leaf.
        if (!bitSet.get(o.bitPosition)) {
            o.bitPosition++;              // feed the next position to the next stack frame by doing computation before preOrder is called.
            node.ch = oisChar.readChar();
            return node;
        } 

        o.bitPosition = o.bitPosition + 1;  // feed the next position to the next stack frame by doing computation before preOrder is called.
        node.left = preOrder(bitSet, oisChar, o); 

        o.bitPosition = o.bitPosition + 1; // feed the next position to the next stack frame by doing computation before preOrder is called.
        node.right = preOrder(bitSet, oisChar, o);

        return node;
    }
    private static void decodeMessage(H_Node node,String File_url) throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(File_url))) {
            final BitSet bitSet = (BitSet) ois.readObject();
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < (bitSet.length() - 1);) {
                H_Node temp = node;
                // since huffman code generates full binary tree, temp.right is certainly null if temp.left is null.
                while (temp.left != null) {
                    if (!bitSet.get(i)) {
                        temp = temp.left;
                    } else {
                        temp = temp.right;
                    }
                    i = i + 1;
               }
                stringBuilder.append(temp.ch);
            }
            
    		String Put_Path = null;
    		String File_name = null;

            //choose a place to save the decompressed file
            JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
			chooser.setFileFilter(filter);
    		int retrival = chooser.showSaveDialog(null);
    	    if (retrival == JFileChooser.APPROVE_OPTION) {
    	    	System.out.println(chooser.getCurrentDirectory().toString());
    	    	Put_Path = chooser.getCurrentDirectory().toString();
    	    	File_name = chooser.getSelectedFile().getName();
    	    	System.out.println(File_name);
    	    } else {
    	        System.out.println("No Selection ");
    	    }
    	    
    	    try (PrintStream out = new PrintStream(new FileOutputStream(Put_Path+"/"+File_name+".txt"))) {
    	        out.print(stringBuilder.toString());
    	    }
        }
    }
    
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException,MalformedURLException 
	{

		Image image = new ImageIcon("res/hufmn.png").getImage();
		Font font1 = new Font("Arial", Font.PLAIN, 25);
		//JFrame implementation
		JFrame frame = new JFrame ("Huffman Zipper");
		frame.setSize(800, 250);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(2,1));
		JButton compress_btn = null;
		JButton decompress_btn = null;
		compress_btn = new JButton("Compress File");
		compress_btn.setBackground(Color.GREEN);
		compress_btn.setFont(font1);
		decompress_btn = new JButton("Decompress File");
		decompress_btn.setBackground(Color.GRAY);
		decompress_btn.setFont(font1);
		frame.add(compress_btn);
		frame.add(decompress_btn);
		frame.setIconImage(image);
		frame.setVisible(true);
		
		//click to compress
		compress_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//browse file to compress
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
				chooser.setFileFilter(filter);
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Browse the file to compress");
			    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);	    
			    chooser.setAcceptAllFileFilterUsed(false);
			    File phile = null;
			    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			    	phile = chooser.getSelectedFile();
			    } else {
			        System.out.println("No Selection ");
			    }

				String content = null;
				try {
					content = new Scanner(phile).useDelimiter("\\Z").next();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					huffman.compress(content);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		});
		
		
		//click to decompress
decompress_btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//browse file to decompress
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("cmp",".cmp","CMP");
				chooser.setFileFilter(filter);
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("Browse the file to decompress");
			    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);	    
			    chooser.setAcceptAllFileFilterUsed(false);
			    File phile = null;
			    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			    	System.out.println(chooser.getCurrentDirectory());
			    	phile = chooser.getSelectedFile();
			    } else {
			        System.out.println("No Selection ");
			    }
				try {
					huffman.expand(chooser.getCurrentDirectory().toString(),phile.getName());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
		});
	}
}
		
