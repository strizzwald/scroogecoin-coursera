import java.util.ArrayList;
import java.util.HashMap;

public class TxHandler {

    private UTXOPool pool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

        // (1)
        for (int i = 0; i < tx.numOutputs(); i++) {
            UTXO utxo = new UTXO(tx.getHash(), i);  

            if (!this.pool.contains(utxo)){
                return false;
            }
        }

        // (2)
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);

            byte[] signature = input.signature;

            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = pool.getTxOutput(utxo);

            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), signature)) {
                return false;
            }
        }

        // (3)
        HashMap<UTXO, Boolean> txUOutputs = new HashMap<>();
        
        for (int i = 0; i < tx.numOutputs(); i++) {
            UTXO o = new UTXO(tx.getHash(), i);

            if (Boolean.FALSE.equals(txUOutputs.get(o))) {
                txUOutputs.put(o, true);
            } else {
                return false; // Double spend.
            }
        }

        // (4)
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false;
            }
        }

        // (5)
        int inputValues = 0;

        for (Transaction.Input input : tx.getInputs()) {
            Transaction.Output output = pool.getTxOutput(new UTXO(input.prevTxHash, input.outputIndex));
            inputValues += output.value;
        }

        int outputValues = 0;

        for (Transaction.Output output : tx.getOutputs()) {
            outputValues += output.value;
        }

        return inputValues >= outputValues;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {

        ArrayList<Transaction> validTransactions = new ArrayList<>();

        for (Transaction t : possibleTxs) {
            if (isValidTx(t)) {
                validTransactions.add(t);

                for (int i = 0; i < t.numInputs(); i++) {
                    UTXO utxo = new UTXO(t.getInput(i).prevTxHash, t.getInput(i).outputIndex);
                    this.pool.removeUTXO(utxo);
                }

                for (int o = 0; 0 < t.numOutputs(); o++) {
                    pool.addUTXO(new UTXO(t.getHash(), o), t.getOutput(o)); 
                }
            }
        }
 
        return validTransactions.toArray(new Transaction[validTransactions.size()]);
    }

}
