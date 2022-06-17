# OBChestShop
Minecraft bukkit/spigot plugin to add a simple sign+chest shop into our Wild Wild West server.
This plugin was inspired by the really nice BetterShops plugin by Max Hubbard.

Requires: Vault and an Economy plugin like Essentials.

Use the /obshop or /obs command to get the command help menu.

## Usage

Place down a Chest, Trapped Chest, Ender Chest, Shulker Box or a Barrel.
Place any type of sign on either face of the chest or on the top in any direction. It is possible to put
up to five shops on a single container. The container is not used for anything presently and the shop
items are stored in the configuration file. Set the first line of the sign text as [obshop]. Put the
shop name on the second line, or enter the shop name in the GUI that opens up when you press Done.
In the shop name GUI enter the name in the text box at the top, then press the paper button on the right
hand side to accept the name of the shop. Names can be up to 35 characters when entered through the GUI
and 23 characters when entered onto the second line of the sign text.

Congratulations, you are now the owner of a shop!

### Navigation and menu layout
Right click the sign to enter the shop main 'sell' screen for the shop, which is where items are sold by
the shop to the player. All screens use the top line for navigating into the different menus available,
and the second line as a divider and for page navigation. All menus, or screens will have a standard set
of buttons on them to allow easy use of the shop. Owners will see extra things to non-owners, such as
the stock room button and will be able to enter the various setting screens. Non-owner can only navigate
between the sell and buy screens.

The main sell screen is indicated by an orange divider and on other menus an orange lantern. Clicking
the orange lantern will always bring you back to the main selling menu. All screens have an arrow in the
top left to navigate back to the previous menu you were on, or exit the shop if on the main sell menu.
There will be a blue lantern on some most screens which lets you access the shop 'buy' menu where the shop
will buy items from players. The 'buy' menu has a blue divider and clicking the blue lantern on any menu
will open the buy menu.

Owners will also see a chest center of the top row which lets them enter the shops store room where they
can manage the shop inventory of items. The store room will have all items being sold or purchased
by the shop.

Most menus will have an ender chest top right of the GUI which lets the owner enter the settings menu for
the particular screen they are on. For example, clicking the ender chest whilst on the stock room menu
will let you change shop setting and manage the stock items. Similarly, clicking on it whilst on the sell
or buy screens will give the shop settings options and allow you to manage the sell and buy items. There
will always be an arrow top left regardless, which will take you back to the previous screen you were on.

A shop sell and buy menus do not support pages just yet, so the maximum number of items you can sell or
buy in the shop is 4 rows of 9 items or 36 items. The stock screen however has page navigation in the
screen divider row, which will let you move back and forth between two pages of items. This is because a
shop can potentially sell 36 items and purchase a completely different 36 items, so it needs to have be
able to move between the two sets of items.

All settings screens whether entered from sell, buy or stock menus, give you the opportunity to do the
following things:

  go back to the main selling screen with the arrow,
  change the name of the shop with the first nametag,
  give the shop a nice description with the second nametag,
  set the shop to Open or Closed with the red/lime green wool, or
  add items to the shop by clicking items in your inventory (sell/stock screens only)

### Change shop name
Simply left click the first nametag and another shop name GUI will open. Enter the name and press the paper that appears
on the right side to accept the new name.
### Change shop description
Left click on the second nametag to enter a new description for the shop. Limited to 35 characters.
### Change shop limit
Left click on the compass to set a shop-wide stock limit. This is a stock limit for any item in the shop and you
will not be able to add stock to the shop beyond that. There is a default setting of 200 and a default absolute maximum
value of 5000, meaning no shop item can go beyond that. These can be changed in the configuration file, but cannot be
changed in-game yet. It's on the to-do list.
### Open and close the shop
If the wool top right on the settings screen is red then the shop is closed. Left click to open the shop to the public.
If the wool top right is green, then clicking it will close the shop and only the owner can access the shop.
### Add items for sale
Navigate: click the ender chest while on the main sell (orange) menu.
Left clicking an item in your inventory will take one out of your inventory and add it to the shop stock. Shift left
clicking an item in your inventory will place that particular stack into stock, and finally right clicking the item
will add all the items in your inventory of that type into stock. This method is also a quick way of topping up the
stock of an item in the shop. Hover the mouse over the shop item to see the stock quantity as well as the price.
### Add items to buy
Navigate: click the ender chest while on the buy (blue) menu.
Similar to adding items for sale, enter the settings menu from the buy menu and click an item in your inventory.
However, no items are actually moved from your inventory into stock for the buy side. It's symbolic and players
actually selling items will increase the stock of that item in the shop.
### Add items to stock directly
Navigate: click the chest to enter the stock screen or click the ender chest to enter settings for the stock menu.
From either the stock menu or settings menu for the stock screen you can top up the stock of an item that is for
sale or purchase. You cannot put items into stock that aren't for sale or purchase. The same approach as the sell
and buy screens where one left click adds one item, a shift-left click adds the stack you clicked on, and a right
click will add all items in your inventory of that type - up to the limit the shop allows.

## Configure a shop item, other ways to add/remove stock, and remove an item from the shop
In all settings menus you can lef click a shop item to configure it. This item configuration screen will let you
modify the price of the item (sell and buy), as well as the description for the item if it's an item for sale.
The shop item configuration screen for sell and stock menus will also allow you to conveniently add or remove stock,
or completely remove the item from the shop. The item configuration menu for items the shop buys doesn't require this.

The three prismarine and three red sandstone blocks let you move quantities back and forth from
inventory to stock and stock to inventory in six different ways.

  slabs  - left click to move 1 into in or out of stock, shift left click to move two items<br>
  stairs - left click to move 25% in or out, and shift left click to move 50% of the stock or inventory in or out<br>
  block  - left click to move 75% in/out, shift left click to move 100%<br>
  
Stock limits will obviously prevent you moving too much inventory into stock, however moving all stock to inventory will
fill up your inventory slots and then dump the rest on the ground, so watch out!

The barrier block top right will remove the item from the shop. This will also dump the entire stock of the item into
the players inventory and any excess will drop on the ground.

That covers the main operation of the shop. If the shop is open players can right click on the sign to open up the main
selling screen. They cannot access the configuration screen, but the ender chest will be there and hovering the mouse
over it will tell them who owns the shop.

## Purchasing and selling items
Simply click on the item in the shop and this will drop you into the item sell or buy screen where the buy or sell
options are presented. The options will be the 5 blocks from left to right in the center of the shop screen for items
you are buying from the shop. There are 6 options for selling to the shop. If the block is green (sell) or blue (buy)
then the option is available, but if white then there is some reason why you can't purchase or sell that option - eg.
lack of money on your or the shop owners part, or lack of stock of the item in the shop. Hover your mouse over the
white wool to see the reason why that option is not available. Hovering your mouse of the green of blue option will
show you how many are being sold/bought and what you will pay/receive.

The options from left to right are:

  1  - buy/sell one item<br>
  8  - buy/sell eight * the price of the item<br>
  16 - buy/sell a quarter stack - 16 * the price<br>
  32 - buy/sell a half stack - 32 items * the price of the item<br>
  64 - buy/sell a full stack - 64 * price<br>
 All - sell all the items in your inventory up to the stock limit for the shop (shop buy only)<br>
 
Obviously, if your inventory space is limited then some items will drop to the floor, so watch out!

## Removing a shop
Breaking a shop sign will result in the shop being removed and the shop stock being placed into the players inventory.
Any excess stock is dropped on the ground. Breaking the last sign on the chest also removes the chest, with any
chest contents going onto the ground (note: chest contents aren't used by the shop or shop stock presently).
Breaking a shop chest will remove ALL signs on the chest and therefore all shops associated with those signs.
A chest can have up to 5 signs/shops on it, but they can only be by the same owner. Alternatively there is a command
to remove a shop by name. See the Commands section for more details.

## Commands
/obs or /obshop will bring up the available commands:<br>
/obs remove <shopname | all> - removes the named shop or all of your shops - obviously must be owned by the remover<br>
/obs list [all] - lists your shops or all shops with the world they exist in<br>
/obs status [all] - lists your shops or all shops along with their state (see notes on state later)<br>
/obs location [all] - lists your shops or all shop along with their world and [X,Y,Z] location<br>
/obs fix <shopname> - attempts to validate and fix a shop (see shop fixing in the technical details section)<br>
/obs autofix [enable|disable] - toggles on/off the scanning and automatic fixing of shops with errors (see shop fixing
  in the technical details)

## Technical details
### Plugin config file
The plugin drops a yaml configuration file into the plugin folder, which has various settings for the plugin to work,
like a default price for a new shop item, shop limit values and a shop check inteval. These can be changed, but should
only be done when the server is offline.
### Shop config file
The plugin will store shop data in yaml files in the plugin/OBChestShop/Shops/<owner uuid> folder. Shops are stored
by shop name. Shop files are updated according to player transactions and setting changes, so editing these whilst
the server is running will result in the changes being overwritten. Shops are also saved on servershutdown.
### Shop state.
The plugin maintains a running state for each shop. Initially when the plugin is loading each shop there is a
'NoShop' state and as each step in validating and loading the shop is performed the state changes until the shop
is at 'ShopOK' state. There are about 19 states a shop can be in, covering everything from config file status, world
 status and sign and chest block validation. This state is used by the shop checking and fix processes.
### Shop fixing or repair
The plugin will periodically run through a health check of a shop and will check the shop state to ensure things
are working fine. The checks include things like validating the world the shop is in still exists, or that the
sign or chest are in the blocks they are supposed to be at the locations specified in the shop config. If shop
fixing is enabled, then the plugin will attempt to fix some of the issues based on what the shop config file
specifies. For example, replacing a sign or block that was removed. Shops that aren't in a valid state of ShopOK
cannot be accessed. The "/obs fix <shopname>" command should be used to correct the shop, or switch on the auto
fix option. The plugin also performs the health check when loading the stop at startup and will attempt to
correct any issues at that time.

## TODO list
The code has a lot of todo's already documented, so next steps are to address those.
In so far as new features we haven't thought that far ahead. We wanted a simple shop that sells items to
replace what we lost when BetterShops stopped being updated. This plugin suited our Wild Wild West server use-case
and probably won't satisfy most other server and player requirements.

Compiled for 1.19 and Java 17.

