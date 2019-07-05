package Viterbi;

public    class TransNode {
    public String source_state;
    double trans_prob;
    public TransNode next_node;
    public TransNode(String ss, double tp){
	source_state = ss;
	trans_prob = tp;
	next_node = null;
    }
}