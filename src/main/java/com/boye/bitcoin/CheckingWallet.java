package com.boye.bitcoin;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;

/**
 * Created by yebo on 2/01/17.
 */
public class CheckingWallet {
    public static void main(String[] args) throws UnreadableWalletException {
        TestNet3Params params = TestNet3Params.get();
        WalletAppKit appKit = new WalletAppKit(params, new File("."), "wallet1"); //The wallet is not to be created twice.
        appKit.startAsync();
        appKit.awaitRunning();
        System.out.println("Network connected!");
        Wallet wallet =  appKit.wallet();
        System.out.println("Wallet's current receive address: " + wallet.currentReceiveAddress());
        System.out.println("Wallet contents: " + wallet);
    }
}