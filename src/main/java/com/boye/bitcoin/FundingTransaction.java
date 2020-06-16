package com.boye.bitcoin;

import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
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

    public FundingTransaction() {
        appKit = new WalletAppKit(params, new File("."), "wallet"); //Loading existing wallet
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

    private void createMultisigAddress() {
        List<ECKey> keys = Arrays.asList(new ECKey(), new ECKey(), new ECKey());
        wallet.importKeys(keys);
        //the below is redeem script
        payingToMultisigTxoutScript = ScriptBuilder.createMultiSigOutputScript(2, keys); //2 of 3 multisig
        System.out.println("Is sent to multisig: " + ScriptPattern.isSentToMultisig(payingToMultisigTxoutScript));
        System.out.println("redeemScript: " + payingToMultisigTxoutScript);

    }

    private void sendCoinsToMultisigAddress() throws InsufficientMoneyException, ExecutionException, InterruptedException {
        Transaction payingToMultisigTx = new Transaction(params);
        Coin value = Coin.valueOf(0, 2); // send 2 cents of BTC
        payingToMultisigTx.addOutput(value, payingToMultisigTxoutScript);
        SendRequest request = SendRequest.forTx(payingToMultisigTx);
        wallet.completeTx(request);
        PeerGroup peerGroup = appKit.peerGroup();
        peerGroup.broadcastTransaction(request.tx).broadcast().get();
        System.out.println("Paying to multisig transaction broadcasted!");
    }
}
