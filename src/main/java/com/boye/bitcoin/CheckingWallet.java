package com.boye.bitcoin;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.Wallet;

import java.io.File;

public class CheckingWallet {
    public static void main(String[] args) {
        TestNet3Params params = TestNet3Params.get();
        WalletAppKit appKit = new WalletAppKit(params, new File("."), "wallet"); //create a new wallet or load an existing wallet.
        appKit.startAsync();
        appKit.awaitRunning();
        System.out.println("Network connected!");
        Wallet wallet = appKit.wallet();
        System.out.println("Wallet's current receive address: " + wallet.currentReceiveAddress());
        System.out.println("Wallet contents: " + wallet);
    }
}