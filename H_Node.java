package huffman;

public class H_Node {
	
	public	char ch;
	public	int freq;
	public	H_Node left;
	public	H_Node right;
	
	//Default Constructor
	H_Node(char ch,int freq,H_Node left,H_Node right)
	{
		this.ch = ch;
		this.freq = freq;
		this.left = left;
		this.right = right;
	}

		

}
