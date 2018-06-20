package com.boye.bitcoin;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FundingTransaction {
    private TestNet3Params params = TestNet3Params.get();
    private WalletAppKit appKit;
    private Wallet wallet;
    private Script payingToMultisigTxoutScript;

    private  void createMultisigAddress(){
        List<ECKey> keys = Arrays.asList(new ECKey(), new ECKey(), new ECKey());
        wallet.importKeys(keys);
        //the below is redeem script, not pay-to-pubkeyhash type, not pay-to-script-hash type
        payingToMultisigTxoutScript = ScriptBuilder.createMultiSigOutputScript(2, keys); //2 of 3 multisig
        System.out.println(payingToMultisigTxoutScript.isPayToScriptHash());
        System.out.println(payingToMultisigTxoutScript.isSentToMultiSig());
        System.out.println("redeemScript: " + payingToMultisigTxoutScript);

    }
    private  void sendCoinsToMultisigAddress() throws InsufficientMoneyException, ExecutionException, InterruptedException {
        Transaction payingToMultisigTx = new Transaction(params);
        Coin value = Coin.valueOf(0,2);
        payingToMultisigTx.addOutput(value, payingToMultisigTxoutScript);
        System.out.println("payingToMultisigTx hash before: "+payingToMultisigTx.getHash());
        SendRequest request = SendRequest.forTx(payingToMultisigTx);
        wallet.completeTx(request); // fill in coins
        System.out.println("payingToMultisigTx hash after: "+payingToMultisigTx.getHash());
        PeerGroup peerGroup = appKit.peerGroup();
        peerGroup.broadcastTransaction(request.tx).broadcast().get();
        System.out.println("Paying to multisig transaction broadcasted!");
        System.out.println("Wallet's current receive address: " + wallet.currentReceiveAddress());
        System.out.println("Wallet contents: " + wallet);
    }

    public FundingTransaction(){
        appKit = new WalletAppKit(params, new File("."), "wallet1"); //The wallet is not to be created twice.
        appKit.startAsync();
        appKit.awaitRunning();
        System.out.println("Network connected!");
        wallet = appKit.wallet();
    }

    public static void main(String[] args) throws InterruptedException, InsufficientMoneyException, ExecutionException {
        FundingTransaction ft = new FundingTransaction();
        ft.createMultisigAddress();
        ft.sendCoinsToMultisigAddress();
    }
}
