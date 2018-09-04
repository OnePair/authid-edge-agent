package ca.onepair.edgeagent.loaders;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.LevelDBBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;

import ca.onepair.authID.AuthIDKeyWallet;
import ca.onepair.authID.BTCAuthIDDriver;
import ca.onepair.authID.SqliteAuthIDKeyWallet;
import ca.onepair.authID.store.AuthIDStore;
import ca.onepair.authID.store.LevelDBAuthIDStore;
import ca.onepair.authid.common.drivers.MasterAuthIDDriver;

public class AuthIDDriverLoader {

	private static final String AGENT_DIR = System.getProperty("user.home") + "/.authid-agent/";

	private static final String BTC_KEY_WALLET = "authid-wallet";;
	private static final String BTC_BLOCKSTORE = "blockstore.spvchain";
	private static final String BTC_AUTHID_STORE = "authids.db";
	private static final String BTC_AUTHID_BLOCKS = "blocks.db";

	public static MasterAuthIDDriver loadDriver()
			throws ClassNotFoundException, SQLException, UnreadableWalletException, IOException, BlockStoreException {
		MasterAuthIDDriver masterDriver = new MasterAuthIDDriver();

		masterDriver.addDriver(AuthIDDriverLoader.loadBTCDriver(), "BTC");

		return masterDriver;
	}

	private static BTCAuthIDDriver loadBTCDriver()
			throws ClassNotFoundException, SQLException, UnreadableWalletException, IOException, BlockStoreException {
		NetworkParameters networkParams = TestNet3Params.get();

		AuthIDKeyWallet keyWallet = new SqliteAuthIDKeyWallet(networkParams,
				AuthIDDriverLoader.getWalletPath(networkParams));
		BlockStore blockStore = AuthIDDriverLoader.getBlockstore(networkParams);
		AuthIDStore authIDStore = AuthIDDriverLoader.getAuthIDStore(networkParams);

		return new BTCAuthIDDriver(networkParams, keyWallet, blockStore, authIDStore);

	}

	private static final File getWalletPath(NetworkParameters networkParams) {
		File walletFolder = new File(AGENT_DIR + "/" + BTC_KEY_WALLET);

		if (!walletFolder.exists())
			walletFolder.mkdirs();

		return walletFolder;
	}

	private static final BlockStore getBlockstore(NetworkParameters networkParams) throws BlockStoreException {
		File blockStoreFile = new File(AGENT_DIR + "/" + BTC_KEY_WALLET + "/" + networkParams.getId() + "/" + BTC_BLOCKSTORE);
		return new LevelDBBlockStore(Context.getOrCreate(networkParams), blockStoreFile);
	}

	private static final AuthIDStore getAuthIDStore(NetworkParameters networkParams) {
		File idStoreFile = new File(AGENT_DIR + "/" + BTC_KEY_WALLET + "/" + networkParams.getId() + "/" + BTC_AUTHID_STORE);
		File blocksFile = new File(AGENT_DIR + "/" + BTC_KEY_WALLET + "/" + networkParams.getId() + "/" + BTC_AUTHID_BLOCKS);

		return new LevelDBAuthIDStore(idStoreFile, blocksFile);
	}

}
