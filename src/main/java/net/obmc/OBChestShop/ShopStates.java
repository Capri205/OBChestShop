package net.obmc.OBChestShop;

public class ShopStates {

    // states a shop can be in - state is set as the shop loads or is created, and by periodic checks
    public enum ShopState {
    	NoShop,																	// initial start for load up of a shop or new shop
    		NoConfig, ConfigSaveFail, ConfigOK,									// shop config file
    		WorldNotExist, WorldOK,												// world related states
    	ShopPreChecks,															// initial load of shop data complete
    		NoChestConfig, ChestConfigOK, NotShopChest, FixChestFail, ChestOK,	// chest config and world block
    		NoSignConfig, SignConfigOK, NotShopSign, FixSignFail, SignOK,		// sign config and world block
        	ShopMAINT, ShopLostOwner,											// shop maintenance mode
    	ShopOK,																	// final ok state

    }
    
}
