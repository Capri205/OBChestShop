# OBChestShop
Minecraft bukkit/spigot plugin to add a simple sign+chest shop into our Wild Wild West server.
This plugin was inspired by the really nice BetterShops plugin by Max Hubbard.

Requires: Vault and an Economy plugin like Essentials.

Use the /obshop or /obs command to get the command help menu.

## Usage

Place down a Chest, Trapped Chest, Ender Chest, Shulker Box or a Barrel.
Place any type of sign on either face of the chest or on the top and set the first line as [obshop].
Optionally you can put the shop name on the second line, or enter the shop name in the GUI that opens up.
In the shop name GUI enter the name in the text box at the top, then press paper on the right side to
accept the name of the shop.

Congratulations, you are now the owner of a shop!

Right click the sign to enter the shop main screen. Initially the shop will be empty and closed to the public.
The top left arrow will close the shop screen, whilst the ender chest will access the shop configuration screen.
When the shop is closed there will be a row of red glass panes across the second row of the screen, and when the
shop is open these will be lime green.

To add some items to the shop for players to purchase or change some settings for the shop, left click the ender chest
to get to the settings screen.

On the shop settings screen you can;
  go back to the main selling screen with the arrow,
  change the name of the shop with the first nametag,
  give the shop a nice description,
  set the shop to Open or Closed with the red/lime green wool, or
  add items to the shop by clicking items in your inventory.

### Change shop name
Simply left click the first nametag and another shop name GUI will open. Enter the name and press the paper that appears
on the right side to accept the new name.
### Change shop description
Left click on the second nametag to enter a new description for the shop. Limited to 35 characters.
### Change shop limit
Left click on the compass to set a shop-wide stock limit. This is a stock limit for any item in the shop and you
will not be able to add stock to the shop beyond that. There is a default setting of 1000 and a default absolute maximum
value of 5000, meaning no shop limit can go beyond that. These can be changed in the configuration file, but cannot be
changed in-game yet. It's on the to-do list.
### Open and close the shop
If the wool top right on the settings screen is red then the shop is closed. Left click to open the shop to the public.
If the wool top right is green, then clicking it will close the shop and only the owner can accessthe shop.
### Add initial items to the shop
Left clicking an item in your inventory whilst in the settings screen will take one out of your inventory and add it
to the shop stock. Shift left clicking an item in your inventory will place that stack, however many items there are
in that stack in your inventory, will be added to what's in the shop stock for that item, or create it as a new item
with that quantity. This is one quick way of topping up the stock of an item in the shop. Hover the mouse over the
shop item to see the stock quantity as well as the price and amount sold per transaction.
## Configure a shop item, add/remove stock and remove an item from the shop
Hovering over the shop item you can see the display telling you to left click the shop item to configure it. This screen
will let you modify the price of the item, the amount sold per transaction/unit price, as well as the description
for the item (called item lore in the display). The shop item configuration screen also allows you to convenitently
add or remove stock and is how to completely remove the item from the shop.

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

## Purchasing items
To purchase simply click on the item in the shop and this will drop you into the main item selling screen where the
options to buy are presented. The options to buy items (shop to sell items) are the 5 blocks from left to right in the
center of the shop screen. If the block is green then the purchase option is available, but if white then there is some
reason why you can't purchase that option - lack of money on your part or lack of stock of the item in the shop. Hover
the mouse over the white wool to see the reason why that option is not available.

The purchase options from left to right are:

  buy 1  - you will pay one and get one of whatever quantity the shop owner has set for one of that item<br>
         - eg. amount is 10 and price is $5 each - you will pay $5 and receive 10 of the item.<br>
         -     no different from buying, say a pack of cookies from the store. You buy 1, but get 10 cookies.<br>
  buy 8  - pay 8 * price and get 8 * the amount - like buying 8 packs of cookies - that's a lot of cookies!<br>
         - eg. amount is 10 and price is $5 - you will pay $40 and receive 80 of the item.<br>
  buy 16 - pay price * 16 and receive the amount * 16 - this is called a quarter stack<br>
         - eg. pay 16 * $5 = $80, and receive 160 of the item.<br>
  buy 32 - pay price * 32 and receive the amount * 32 - this is called a half stack in the plugin<br>
         - eg. pay 32 * $5 = $160, and receive 320 of the item into your inventory - good for stone, not cookies!<br>
  buy 64 - pay price * 64 and receive the amount * 64 - this is called a stack<br>
         - eg. pay 64 * $5 = $320, and receive a whopping 640 of the item!<br>

Obviously, if your inventory space is limited then some items will drop to the floor, so watch out!

## Removing a shop
Breaking a shop sign will result in the shop being removed and the shop stock being placed into the players inventory.
Any excess stock is dropped on the ground. Breaking the last sign on the chest also removes the chest. Breaking a shop
chest will remove ALL signs on the chest and therefore all shops associated with those signs. A chest can have 5
signs/shops on it, but they can only be by the same owner. Alternatively there is a command to remove a shop by name.
See the Commands section for more details.

## Commands
/obs or /obshop will bring up the available commands:<br>
/obs remove <shopname | all> - removes the named shop or all of your shops - obviously must be owned by the remover.<br>
/obs list [all] - lists your shops or all shops with the world they exist in<br>
/obs status [all] - lists your shops or all shops along with their state (see notes on state later)<br>
/obs fix <shopname> - attempts to validate and fix a shop (see notes on shop persistence later)<br>
/obs autofix [enable|disable] - toggles on/off the periodic scanning and automatic fixing of shops with errors (see
notes on shop persistence later)

## Technical details
### Plugin config file
The plugin drops a yaml configuration file into the plugin folder, which has various settings for the plugin to work,
like a default price for a new shop item, shop limit values and a shop check inteval. These can be changed, but should
only be done when the server is offline.
### Shop config file
The plugin will store shop data in yaml files in the plugin/OBChestShop/Shops/<owner uuid> folder. Shops are stored
by shop name. Shop files are updated according to transaction and changes, so editing these whilst the server is
running will result in the changes being overwritten. Shops are also saved on shutdown.
### Shop state.
The plugin maintains a running state for each shop. Initially when the plugin is loading each shop there is a
NoShop state, and as each step in validating and loading the shop is performed the state changes until the shop
is at ShopOK state. There are about 19 states covering config file status, world status to sign and chest block
validation. This state is used by the shop checking and fix processes.
### Shop fixing or repair
The plugin will periodically run through a health check of a shop and will check the shop state to ensure things
are working fine. The checks include things like validating the world the shop is in still exists, or that the
sign or chest are in the blocks they are supposed to be at the locations specified in the shop config. If shop
fixing is enabled, then the plugin will attempt to fix some of the issues based on what the shop config file
specifies. For example, replacing a sign or block that was removed. Shops that aren't in a valid state of ShopOK
cannot be accessed. The "/obs fix <shopname>" command should be used to correct the shop, or switch on the auto
fix option. The plugin also performs the health check when loading the stop at startup and will attempt to
correct any issues at that time.

<<<<<<< HEAD
Compiled for 1.16 using Java 11 (with java 1.8 source version), but should work with Java 8 and older versions of Minecraft (up to a point).
=======
## TODO list
The code has a lot of todo's already documented, so next steps are to address those.
In so far as new features we haven't thought that far ahead. We wanted a simple shop that sells items to
replace what we lost when BetterShops stopped being updated. This plugin is very much our server use-case and
probably won't satisfy most other server and player requirements.

Compiled for 1.16, but should work with older versions (up to a point).
>>>>>>> branch 'master' of https://github.com/Capri205/OBChestShop.git
