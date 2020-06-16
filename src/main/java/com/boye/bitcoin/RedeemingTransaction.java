package com.boye.bitcoin;

import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkState;

public class RedeemingTransaction {
    private TestNet3Params params = TestNet3Params.get();
    private WalletAppKit appKit;
    private Wallet wallet;
    private TransactionOutput multisigOutput;
    private Transaction redeemingMultisigTx1;
    private Transaction redeemingMultisigTx2;
    private TransactionInput redeemMultisigTxInput;
    private ECKey.ECDSASignature partyASignature;
    private ECKey.ECDSASignature partyBSignature;

    public RedeemingTransaction() {
        appKit = new WalletAppKit(params, new File("."), "wallet");
        appKit.startAsync();
        appKit.awaitRunning();
        System.out.println("Network connected!");
        wallet = appKit.wallet();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        RedeemingTransaction rt = new RedeemingTransaction();
        rt.createRawTransactionForMultisigRedeemingByA();
        rt.signRawTransactionForMultisigRedeemingByA();
        rt.createRawTransactionForMultisigRedeemingByB();
        rt.signRawTransactionForMultisigRedeemingByB();
        rt.createAndBroadcastMultisigRedeemingTx();
    }

    private void createRawTransactionForMultisigRedeemingByA() {
        redeemingMultisigTx1 = new Transaction(params);
        multisigOutput = wallet.getUnspents().get(0).getParentTransaction().getOutputs().stream().filter(unspent -> ScriptPattern.isSentToMultisig(unspent.getScriptPubKey())).findFirst().get();
        System.out.println("multisigOutput: " + multisigOutput);
        redeemingMultisigTx1.addInput(multisigOutput);
        Coin value = multisigOutput.getValue();
        wallet = appKit.wallet();
        Address finalAddress = wallet.currentReceiveAddress();
        redeemingMultisigTx1.addOutput(value.div(2), finalAddress);   //leave some mining fee
        System.out.println("Send to final address: " + finalAddress);
    }

    private void signRawTransactionForMultisigRedeemingByA() {
        Script payingToMultisigTxoutScriptPubKey = multisigOutput.getScriptPubKey();
        System.out.println("payingToMultisigTxoutScriptPubKey: " + payingToMultisigTxoutScriptPubKey);
        checkState(ScriptPattern.isSentToMultisig(payingToMultisigTxoutScriptPubKey));
        Sha256Hash sighash = redeemingMultisigTx1.hashForSignature(0, payingToMultisigTxoutScriptPubKey, Transaction.SigHash.ALL, false);
        partyASignature = wallet.getImportedKeys().get(0).sign(sighash);
    }

    private void createRawTransactionForMultisigRedeemingByB() {
        redeemingMultisigTx2 = new Transaction(params);
        redeemMultisigTxInput = redeemingMultisigTx2.addInput(multisigOutput);
        Coin value = multisigOutput.getValue();
        wallet = appKit.wallet();
        Address finalAddress = wallet.currentReceiveAddress();
        redeemingMultisigTx2.addOutput(value.div(2), finalAddress);
        System.out.println("Send to final address: " + finalAddress);
    }

    private void signRawTransactionForMultisigRedeemingByB() {
        Script payingToMultisigTxoutScriptPubKey = multisigOutput.getScriptPubKey();
        Sha256Hash sighash = redeemingMultisigTx2.hashForSignature(0, payingToMultisigTxoutScriptPubKey, Transaction.SigHash.ALL, false);
        partyBSignature = wallet.getImportedKeys().get(1).sign(sighash);

    }

    private void createAndBroadcastMultisigRedeemingTx() throws ExecutionException, InterruptedException {
        TransactionSignature signatureA = new TransactionSignature(partyASignature, Transaction.SigHash.ALL, false);
        TransactionSignature signatureB = new TransactionSignature(partyBSignature, Transaction.SigHash.ALL, false);
        Script inputScript = ScriptBuilder.createMultiSigInputScript(signatureA, signatureB);
        System.out.println("redeeming Tx input script: " + inputScript);
        redeemMultisigTxInput.setScriptSig(inputScript);
        redeemMultisigTxInput.verify(multisigOutput);
        PeerGroup peerGroup = appKit.peerGroup();
        peerGroup.broadcastTransaction(redeemingMultisigTx2).broadcast().get();
        System.out.println("Multisig redeeming transaction broadcasted!");
    }
}
