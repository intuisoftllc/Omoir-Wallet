# Plaid-Crypto-Wallet
An open source bitcoin wallet


[![Playstore](https://upload.wikimedia.org/wikipedia/commons/7/78/Google_Play_Store_badge_EN.svg)](https://play.google.com/store/apps/details?id=com.intuisoft.plaid)

Website: [plaidcryptowallet](https://plaidcryptowallet.com)

* Private keys never leave your device
* Asset Transfer Protocol. See Below
* SegWit Support
* Encrypted Wallet Data
* And much more [features...](https://plaidcryptowallet.com)

<img src="https://github.com/intuisoftllc/Plaid-Crypto-Wallet/blob/main/docs/pictures/phone_app.svg" width="100%" height="600">

#### Cryptocurrency Support
Keep in mind that bitcoin is the only cryptocurrency supported currently, however support for other currencies will be coming in the future!

## Asset Transfer Protocol
Plaid Crypto Wallet is the first ever mobile wallet to have a dedicated in-app BTC UTXO management system called Asset Transfer Protocol (ATP)
This works by individually sending yor UTXO's from one location to another in a series of "Batches". This effectiveley allows you to "transfer"
your wallet with some or all of its contents without having to make a series of single transactions "doxxing" the coins that were involed in the transaction.
Below is an example of how this process works:

```
Utxo's in wallet A        Utxo's in recepient wallet B

Batch #1 @block 750001
[m/0]  bc1qvcx...   ----> [m/0] bc1qycx...    
[m/1]  bc1q5ct...   ----> [m/1] bc1qexb...      
[m/2]  bc1qqrx...   ----> [m/2] bc1qaca...

2 Block Batch Gap
@block 750002 - skipped
@block 750003 - skipped

Batch #2 @block 750004
[m/3]  bc1qccx...   ----> [m/3] bc1qf4cx...    
[m/4]  bc1qxxt...   ----> [m/4] bc1qeab...      
[m/5]  bc1qqex...   ----> [m/5] bc1qada...

2 Block Batch Gap
@block 750005 - skipped
@block 750006 - skipped

batch #3
...
```

Above we create a simple ATP transaction with a batch gap of 2 (skip 2 blocks per batch) and a batch size of 3 (3 UTXO's per batch).
The batch gaps provide more anonymity and the batch size can be any number of UTXO's up to 50.

## RESPONSIBLE DISCLOSURE

Found critical bugs/vulnerabilities? Please email them to support@plaidcryptowallet.com
Thanks!
