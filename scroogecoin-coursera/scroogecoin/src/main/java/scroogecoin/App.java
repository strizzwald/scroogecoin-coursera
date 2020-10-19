package scroogecoin;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.math.BigInteger;
import java.security.*;

/**
 * https://gist.github.com/mentlsve/ef15013f1e6e5abd82996b34a7b4131b
 *
 */
public class App 
{
    public static void main( String[] args )  throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        
        // Crypto setup
        // You need the following JAR for RSA http://www.bouncycastle.org/download/bcprov-jdk15on-156.jar
        // More information https://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)
        
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);

        // Generating two key pairs, one for Scrooge and one for Alice
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey private_key_scrooge = pair.getPrivate();
        PublicKey public_key_scrooge = pair.getPublic();

        pair = keyGen.generateKeyPair();
        PrivateKey private_key_alice = pair.getPrivate();
        PublicKey public_key_alice = pair.getPublic();

        // START - ROOT TRANSACTION
        // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
        // By thin air I mean that this tx will not be validated, I just need it to get a proper Transaction.Output
        // which I then can put in the UTXOPool, which will be passed to the TXHandler
        Transaction tx = new Transaction();
        tx.addOutput(10, public_key_scrooge);

        // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
        byte[] initialHash = BigInteger.valueOf(1695609641).toByteArray();
        tx.addInput(initialHash, 0);
    }
}
