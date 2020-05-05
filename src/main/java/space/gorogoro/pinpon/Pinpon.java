package space.gorogoro.pinpon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Pinpon extends JavaPlugin implements Listener{

  @Override
  public void onEnable(){
    try{
      getServer().getPluginManager().registerEvents(this, this);
      getLogger().info("The Plugin Has Been Enabled!");

      // 設定ファイルが無ければ作成
      File configFile = new File(this.getDataFolder() + "/config.yml");
      if(!configFile.exists()){
        this.saveDefaultConfig();
      }

    } catch (Exception e){
      getLogger().warning(e.toString());
    }

  }

  /**
   * コマンド実行時に呼び出されるメソッド
   * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
   *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
   */
  public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
    try{
      if(sender instanceof BlockCommandSender) {
        String spName = "";
        double d =0,preD = 0;
        Block b = ((BlockCommandSender) sender).getBlock();
        for(Entity e: b.getChunk().getEntities()) {

          if((e instanceof Player) == false) {
            continue;
          }

          d = b.getLocation().distance(e.getLocation());
          if(d > 7) {
            continue;
          }

          if(preD == 0 || preD > d) {
            spName = e.getName();
            preD = d;
          }
        }
        
        if( command.getName().equals("pinpon") && args.length == 1) {
          
          // 20ticks後に1度だけ実行される処理を、実装しつつ、そのままスケジュールします。
          new BukkitRunnable() {
   
            public String spName;
            
            @Override
            public void run() {
              try {
                FileConfiguration conf = getConfig();
                String strUrl = conf.getString("api-url");
                URL url = new URL(strUrl);
                HttpURLConnection con = null;
                con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8));
                String strPost = "data=" + args[0] + "&sender=" + spName;
                writer.write(strPost);
                writer.write("\r\n");
                writer.flush();
                if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                }
                con.disconnect();
              }catch(Exception e) {
              }
            }
            
            public BukkitRunnable setSpName(String str) {
              spName = str;
              return this;
            }
          }.setSpName(spName).runTaskLaterAsynchronously(this, 20);

          return true;
        }
      }
      return false;
    }catch(Exception e){
      getLogger().warning(e.toString());
    }
    return true;
  }

  @Override
  public void onDisable(){
    getLogger().info("The Plugin Has Been Disabled!");
  }
}
