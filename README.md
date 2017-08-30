# BlacklistSRVBypass
This is a Minecraft SRV Record Automatic Updater created by DataPools. This is only compatible with Cloudflare and Cloudflare API v4. The program automatically changes the SRV record inside of a domain to an unblacklisted domain that is specified within domains.txt. I used Unirest as a Rest API and Gson to parse and process Json.

## COMPILATION
This requires Java 8. Dependencies are Google Gson and Mashape Unirest. 

## CONFIGURATION

For Mac users, do not use TextEdit to edit config.json. It will cause errors in the program.

All configuration is done via the "config.json" file created on first startup. X-Auth-Email is the email used on your Cloudflare login. X-Auth-Key is the api key that you generated on your account.

This program is only for domains that use 25565 as their port. 

Zone ID is the id of your dns record zone. It can be found by running a GET request and looking for an SRV record. Here is a semi-tutorial https://kojiroh.wordpress.com/2016/07/26/how-to-find-cloudflare-zone-id/.

Record id is known as zone identifier in this article https://support.cloudflare.com/hc/en-us/articles/203702810-Host-Partner-Full-Zone-Set-Up-API-Instructions

Keep in mind this program requires and EXISTING SRV record that is already functioning. The program automatically updates the SRV record when executed

Get the proper Record ID for the SRV Record you want.

Input all of this information in their respective fields inside of config.json

domains.txt file contains a list of domains that can be used inside of the SRV record.

The program automatically gets the domain closest to the top of the file that is not blacklisted. It checks the blacklist through the blacklist api on http://mcapi.ca. If it matches the current target, no changes will be made.

Make sure to load your domains.txt if you are frequently blacklisted by Mojang. If all domains inside the file are blacklisted, the program will error.

## RUNNING

Run the program through the Terminal. It has NO GUI, so you won't be able to see what is going on without executing it through a terminal. A cron job would probably be the best choice if you want to constantly update your SRV records if your server is getting blacklisted.

## DISTRIBUTION

Feel free to distribute the program. Just give credit. Screw Mojang!
