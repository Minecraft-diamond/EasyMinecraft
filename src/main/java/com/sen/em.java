package com.sen;

import com.sen.Log.Log;
import com.sen.QuestionnaireCore.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.sen.Toolkit.*;

public final class em extends JavaPlugin {

    public static List<com.sen.Command> registerCommands = new ArrayList<>();

    public static em getInstance() {
        return JavaPlugin.getPlugin(em.class);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println(" -- Thanks for using this plugin. -- ");
        System.out.println(" -- This plugin is free at all. -- ");
        System.out.println(" -- Author: SentientRook131 -- ");
        System.out.println(" -- QQ: 3460596497 -- ");
        System.out.println(" -- Please make sure that this server can get the Real Address of players -- ");
        saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        Function<Pair<String[], CommandSender>, Boolean> f = a -> {
            a.second.sendMessage(prefix + "本指令为测试指令，您输入的参数为" + Arrays.toString(a.first));
            return true;
        };
        registerCommand("test", f);
        List<Long> questionnaireIds = config.getLongList("questionnaires.$registry$");
        for (long questionnaireId : questionnaireIds) {
            long id = config.getLong("questionnaires." + questionnaireId + ".id");
            String description = config.getString("questionnaires." + questionnaireId + ".description");
            String title = config.getString("questionnaires." + questionnaireId + ".title");
            Questionnaire questionnaire = new Questionnaire(title, id, description);
            List<Long> questionIds = config.getLongList("questionnaires." + questionnaireId + ".$registry$");
            for (long questionId : questionIds) {
                String qName = config.getString("questionnaires." + questionnaire.id + ".questions." + questionId + ".name");
                String qDescription = config.getString("questionnaires." + questionnaire.id + ".questions." + questionId + ".description");
                int choices_count = config.getInt("questionnaires." + questionnaire.id + ".questions." + questionId + ".choices_count");
                List<Choice> qChoices = new ArrayList<>();
                for (int i = 0; i < choices_count; i++) {
                    String choiceEssential = config.getString("questionnaires." + questionnaire.id + ".questions." + questionId + ".choices." + i + ".essential");
                    String choiceView = config.getString("questionnaires." + questionnaire.id + ".questions." + questionId + ".choices." + i + ".view");
                    Choice choice = new Choice(choiceEssential, choiceView);
                    qChoices.add(choice);
                }
                boolean qIsAnswerable = config.getBoolean("questionnaires." + questionnaire.id + ".questions." + questionId + ".isAnswerable");
                int qScore = config.getInt("questionnaires." + questionnaire.id + ".questions." + questionId + ".score");
                String qTypeStr = config.getString("questionnaires." + questionnaire.id + ".questions." + questionId + ".type");
                QuestionType qType = qTypeStr.equalsIgnoreCase("CHOICE") ? QuestionType.CHOICE : qTypeStr.equalsIgnoreCase("CHOICE") ? QuestionType.COMPLETION : QuestionType.SHORT_ANSWER;
                String qAnswer = config.getString("questionnaires." + questionnaire.id + ".questions." + questionId + ".answer");
                Question question = new Question(qIsAnswerable, qType, qName, questionId, qDescription, qAnswer, qChoices, qScore);
                questionnaire.addQuestion(question);
            }
            registerQuestionnaire(questionnaire);
        }
        if (questionnaires == null) questionnaires = new ArrayList<>();
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        List<Long> questionnaireIds = new ArrayList<>();
        for (Questionnaire questionnaire : questionnaires) {
            questionnaireIds.add(questionnaire.id);
            config.set("questionnaires." + questionnaire.id + ".id", questionnaire.id);
            config.set("questionnaires." + questionnaire.id + ".description", questionnaire.description);
            config.set("questionnaires." + questionnaire.id + ".title", questionnaire.title);
            List<Long> questionIds = new ArrayList<>();
            for (Question question : questionnaire.questions) {
                questionIds.add(question.id);
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".name", question.name);
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".description", question.description);
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".type", question.type.toString());
                for (int i = 0;i < question.choices.size();i++) {
                    config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".choices." + i + ".essential", question.choices.get(i).essential);
                    config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".choices." + i + ".view", question.choices.get(i).view);
                }
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".choices_count", question.choices.size());
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".choices", question.choices);
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".isAnswerable", question.isAnswerable);
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".score", question.score);
                config.set("questionnaires." + questionnaire.id + ".questions." + question.name + ".answer", question.answer);
            }
            config.set("questionnaires." + questionnaire.id + ".$registry$", questionIds);
        }
        config.set("questionnaires.$registry$", questionnaireIds);
        /*
        LocalDateTime now = LocalDateTime.now();
        try {
            log.saveToFile("./logs/log-" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth() + "-" + now.getHour() + "-" + now.getMinute() + "-" + now.getSecond() + ".txt");
        } catch (IOException e) {
            System.out.println("日志文件保存失败！");
        }*/
        saveConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;
        try {
            if (cmd.getName().equalsIgnoreCase("em")) {
                if (args[0].equalsIgnoreCase("location-display")) {
                    if (args[1].equalsIgnoreCase("off")) {
                        config.set("location-display.players-settings." + player.getUniqueId() + ".show-mode", "off");
                    } else if (args[1].equalsIgnoreCase("province")) {
                        config.set("location-display.players-settings." + player.getUniqueId() + ".show-mode", "province");
                    } else if (args[1].equalsIgnoreCase("city")) {
                        config.set("location-display.players-settings." + player.getUniqueId() + ".show-mode", "city");
                    } else if (args[1].equalsIgnoreCase("country")) {
                        config.set("location-display.players-settings." + player.getUniqueId() + ".show-mode", "country");
                    } else if (args[1].equalsIgnoreCase("reload")) {
                        player.sendMessage(prefix + "正在重新获取位置信息。");
                        config.set("location-display.players-settings." + player.getUniqueId() + ".location-buffer", getLocationInfo(player.getAddress()));
                        player.sendMessage(prefix + "获取成功。");
                    } else if (args[1].equalsIgnoreCase("disable") ) {
                        if (!sender.isOp()) {
                            sender.sendMessage(prefix + "您不是管理员，无法使用此命令！");
                            return true;
                        }
                        config.set("location-display.enabled", false);
                        sender.sendMessage(prefix + "设置全局禁用成功！");
                    } else if (args[1].equalsIgnoreCase("enable")) {
                        if (!sender.isOp()) {
                            sender.sendMessage(prefix + "您不是管理员，无法使用此命令！");
                            return true;
                        }
                        config.set("location-display.enabled", true);
                        sender.sendMessage(prefix + "设置全局启用成功！");
                    } else {
                        sender.sendMessage(prefix + "输入的参数有问题！如需关闭请输入参数：off。");
                    }
                } else if (args[0].equalsIgnoreCase("var")) {
                    if (args[1].equalsIgnoreCase("create")) {
                        if (args[2].equalsIgnoreCase("public")) {
                            if (!sender.isOp()) {
                                sender.sendMessage(prefix + "没有权限添加公有变量！");
                                return true;
                            }
                            if (config.getStringList("variables.public.$defined$").contains(args[3])) {
                                sender.sendMessage(prefix + "变量已存在！");
                            } else {
                                config.set("variables.public." + args[3], (args.length == 4) ? "" : args[4]);
                                List<String> list = config.getStringList("variables.public.$defined$");
                                list.add(args[3]);
                                config.set("variables.public.$defined$", list);
                                sender.sendMessage(prefix + "变量创建成功！");
                            }
                        } else if (args[2].equalsIgnoreCase("private")) {
                            if (config.getStringList("variables.private." + player.getUniqueId() + ".$defined$").contains(args[3])) {
                                player.sendMessage(prefix + "变量已存在！");
                            } else {
                                config.set("variables.private." + player.getUniqueId() + "." + args[3], (args.length == 4) ? "" : args[4]);
                                List<String> list = config.getStringList("variables.private." + player.getUniqueId() + ".$defined$");
                                list.add(args[3]);
                                config.set("variables.private." + player.getUniqueId() + ".$defined$", list);
                                player.sendMessage(prefix + "变量创建成功！");
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("set")) {
                        List<String> public_variables = config.getStringList("variables.public.$defined$");
                        List<String> private_variables = config.getStringList("variables.private." + player.getUniqueId() + ".$defined$");
                        if (private_variables.contains(args[2])) config.set("variables.private." + player.getUniqueId() + "." + args[2], args[3]);
                        else if (public_variables.contains(args[2])) {
                            if (sender.isOp()) {
                                config.set("variables.public." + args[2], args[3]);
                            } else {
                                sender.sendMessage(prefix + "没有权限设置公有变量！");
                            }
                        }
                        else {
                            sender.sendMessage(prefix + "变量不存在！");
                            return true;
                        }
                        sender.sendMessage(prefix + "变量设置成功！");
                    }
                } else if (args[0].equalsIgnoreCase("toolkit")) {
                    if (args[1].equalsIgnoreCase("ping")) {
                        if (args.length == 2) player.sendMessage(prefix + "您的延迟：" + player.getPing() + "ms");
                        else player.sendMessage(prefix + args[2] + "的延迟：" + Objects.requireNonNull(Bukkit.getServer().getPlayer(args[2])).getPing() + "ms");
                    } else if (args[1].equalsIgnoreCase("random-tp")) {
                        Random random = new Random();
                        int x = random.nextInt(-10000000,10000000);
                        int z = random.nextInt(-10000000, 10000000);
                        int y = player.getWorld().getHighestBlockYAt(x, z) + 1;
                        player.teleport(new Location(player.getWorld(), x, y, z));
                        player.sendMessage(prefix + "随机传送成功！");
                    } else if (args[1].equalsIgnoreCase("random-num")) {
                        Random random = new Random();
                        if (args.length == 3) {
                            int rn = random.nextInt(0, Integer.parseInt(args[2]));
                            for (Player p : player.getWorld().getPlayers()) {
                                p.sendMessage(prefix + "玩家 " + player.getDisplayName() + " 抽中了随机数：" + rn);
                            }
                        } else if (args.length == 4) {
                            int min = Integer.parseInt(args[2]);
                            int max = Integer.parseInt(args[3]);
                            if (max <= min) {
                                player.sendMessage(prefix + "最大值不能小于最小值！");
                                return true;
                            }
                            int rn = random.nextInt(min, max);
                            for (Player p : player.getWorld().getPlayers()) {
                                p.sendMessage(prefix + "玩家 " + player.getDisplayName() + " 抽中了随机数：" + rn);
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("questionnaire")) {
                    if (args[1].equalsIgnoreCase("conduct")) {
                        String title = args[2];
                        if (args.length == 4) player = Bukkit.getPlayer(args[3]);
                        Questionnaire q = titleMatchesQuestionnaire(title);
                        QuestionnaireInstance qi = q.conduct(player, true, true, true, true);
                        qi.start();
                    }
                } else if (args[0].equalsIgnoreCase("give-command-permission") || args[0].equalsIgnoreCase("gcp")) {
                    if (!sender.isOp()) {
                        sender.sendMessage(prefix + "您不是管理员，无法允许别人使用命令");
                        return true;
                    }
                    String allow = args[1];
                    long time = Long.parseLong(args[2]);
                    String[] commands = Arrays.copyOfRange(args, 3, args.length);
                    sender.sendMessage(prefix + "允许" + allow + "的指令：" + "/" + String.join(" ", commands));
                    UUID uuid = Bukkit.getPlayer(allow).getUniqueId();
                    List<String[]> original = allowCommands.get(uuid);
                    original.add(commands);
                    allowCommands.put(uuid, original);
                    Bukkit.getScheduler().runTaskTimer(this, (task) -> {
                        List<String[]> o = allowCommands.get(uuid);
                        o.remove(commands);
                        allowCommands.put(uuid, o);
                        task.cancel();
                    }, time * 20, 0);
                } else if (args[0].equalsIgnoreCase("run_command")) {
                    String[] finalCommandsList = Arrays.copyOfRange(args, 1, args.length);
                    String command = String.join(" ", finalCommandsList);
                    if (!allowCommands.isEmpty() && allowCommands.containsKey(player.getUniqueId())) {
                        List<String[]> taps = allowCommands.get(player.getUniqueId());
                        Optional<String[]> o = taps.stream().filter(s -> {
                            AtomicBoolean b = new AtomicBoolean(true);
                            try {
                                for (int i = 1;i < s.length + 1;i++) {
                                    if (!args[i].equals(s[i - 1]) && !s[i - 1].equals("*")) {
                                        String[] s2 = s[i - 1].split("\\|");
                                        int finalI = i;
                                        b.set(Arrays.stream(s2).anyMatch(str -> str.equals(args[finalI])));
                                    }
                                }
                            } catch (Exception ignore) {
                                b.set(false);
                            }
                            return b.get();
                        }).findFirst();
                        if (o.isPresent()) {
                            boolean isOp = player.isOp();
                            player.sendMessage(prefix + "您将要执行：/" + command);
                            player.setOp(true);
                            this.getServer().dispatchCommand(player, command);
                            player.setOp(isOp);
                        }else {
                            player.sendMessage(prefix + "您没有使用此命令的权限！");
                        }
                    }

                } else if (args[0].equalsIgnoreCase("remove-command-permission") || args[0].equalsIgnoreCase("rcp")) {
                    String who = args[1];
                    String root = args[2];
                    UUID uuid = Bukkit.getPlayer(who).getUniqueId();
                    List<String[]> allow = allowCommands.get(uuid);
                    if (allow.removeIf(s -> s[0].equals(root))) sender.sendMessage(prefix + "移除玩家执行" + root + "指令的权限成功！");
                    else sender.sendMessage(prefix + "移除失败，可能是本身就不具有执行此指令的权限！");
                    allowCommands.put(uuid, allow);
                } else {
                    Optional<com.sen.Command> optionalCommand = registerCommands.stream()
                            .filter(command -> command.rootCmd.equalsIgnoreCase(args[0]))
                            .findFirst();

                    if (optionalCommand.isPresent()) {
                        com.sen.Command command = optionalCommand.get();
                        try {
                            return command.run(Arrays.copyOfRange(args, 1, args.length), sender);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return true;
                        }
                    }
                    player.sendMessage(prefix + "未找到指令：" + args[0]);
                    return true;
                }
            }
        } catch (Exception ex) {
            player.sendMessage(prefix + "输入的参数有问题！");
            throw new RuntimeException(ex);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command cmd, @NotNull String alias, String[] args) {
        List<String> result = new ArrayList<>();
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (cmd.getName().equalsIgnoreCase("em")) {
            if (args.length == 1) {
                result.add("location-display");
                result.add("var");
                result.add("toolkit");
                result.add("questionnaire");
                result.add("run_command");
                if (sender.isOp()) {
                    result.add("give-command-permission");
                    result.add("gcp");
                    result.add("remove-command-permission");
                    result.add("rcp");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("location-display")) {
                    result.add("off");
                    result.add("reload");
                    result.add("city");
                    result.add("province");
                    result.add("country");
                    if (sender.isOp()) {
                        if (config.getBoolean("location-display.enabled")) {
                            result.add("disable");
                        } else {
                            result.add("enable");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("var")) {
                    result.add("create");
                    result.add("set");
                } else if (args[0].equalsIgnoreCase("toolkit")) {
                    result.add("ping");
                    result.add("random-tp");
                    result.add("random-num");
                } else if (args[0].equalsIgnoreCase("questionnaire")) {
                    result.add("conduct");
                    result.add(ChatColor.GREEN + "由于技术原因，暂不支持游戏内创建问卷，请自行编程创建问卷！");
                } else if (args[0].equalsIgnoreCase("run_command")) {
                    if (!allowCommands.isEmpty() && allowCommands.containsKey(player.getUniqueId())) allowCommands.get(player.getUniqueId()).forEach(s -> result.add(s[0]));
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("var")) {
                    if (args[1].equalsIgnoreCase("create")) {
                        result.add("public");
                        result.add("private");
                    }
                } else if (args[0].equalsIgnoreCase("toolkit")) {
                    if (args[1].equalsIgnoreCase("ping")) {

                    } else if (args[1].equalsIgnoreCase("random-num")) {
                        result.add("[min]");
                        result.add("<max>");
                    }
                } else if (args[0].equalsIgnoreCase("questionnaire")) {
                    if (args[1].equalsIgnoreCase("create")) {
                        result.add("不受支持！");
                    } else if (args[1].equalsIgnoreCase("add_question")) {
                        result.add("不受支持！");
                    } else if (args[1].equalsIgnoreCase("conduct")) {
                        for (Questionnaire q : questionnaires) result.add(q.title);
                    }
                } else if (args[0].equalsIgnoreCase("give-command-permission") || args[0].equalsIgnoreCase("gcp")) {
                    result.add("time");
                } else if (args[0].equalsIgnoreCase("remove-command-permission") || args[0].equalsIgnoreCase("rcp")) {
                    allowCommands.get(Bukkit.getPlayer(args[1]).getUniqueId()).forEach(s -> result.add(s[0]));
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("questionnaire")) {
                    if (args[1].equalsIgnoreCase("create")) {
                        result.add("<name>");
                    } else if (args[1].equalsIgnoreCase("add_question")) {
                        result.add("<title>");
                    } else {
                        result.add(ChatColor.LIGHT_PURPLE + "Tab补全不可用！");
                    }
                } else if (args[0].equalsIgnoreCase("toolkit")) {
                    if (args[1].equalsIgnoreCase("random-num")) {
                        result.add("[max]");
                    }
                }
            } else if (args.length == 5) {
                if (args[0].equalsIgnoreCase("questionnaire")) {
                    if (args[1].equalsIgnoreCase("create")) {
                        result.add("<description>");
                    } else if (args[1].equalsIgnoreCase("add_question")) {
                        result.add("<description>");
                    } else {
                        result.add(ChatColor.LIGHT_PURPLE + "Tab补全不可用！");
                    }
                }
            } else if (args.length == 6) {
                if (args[1].equalsIgnoreCase("add_question")) {
                    result.add("CHOICE");
                    result.add("COMPLETION");
                    result.add("SHORT_ANSWER");
                }
            } else if (args.length == 7) {
                if (args[1].equalsIgnoreCase("add_question")) {
                    result.add("<answer>");
                }
            } else if (args.length == 8) {
                if (args[1].equalsIgnoreCase("add_question")) {
                    result.add("<score>");
                }
            } else {
                if (args[1].equalsIgnoreCase("add_question")) {
                    result.add("<choices>");
                } else result.add(ChatColor.LIGHT_PURPLE + "Tab补全不可用！");
            }
            if (args.length >= 4) {
                if (args[0].equalsIgnoreCase("give-command-permission") || args[0].equalsIgnoreCase("gcp")) {
                    result.add("command");
                    result.add("*");
                }
            }
            if (args.length >= 3) {
                if ((args[0].equals("give-command-permission") || args[0].equals("gcp")) && !allowCommands.isEmpty() && allowCommands.containsKey(player.getUniqueId())) {
                    List<String[]> taps = allowCommands.get(player.getUniqueId());
                    taps.forEach(s -> {
                        try {
                            for (int i = 2;i < Math.min(args.length, s.length + 2);i++) {
                                if (args[i].equals(s[i - 2]) || s[i - 2].equals("*")) {
                                    result.add(s[i - 1]);
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception ignore) {
                            result.add("Tab补全错误");
                        }
                    });
                }
            }
        }
        return result.isEmpty() ? super.onTabComplete(sender, cmd, alias, args) : result;
    }

    public boolean registerCommand(String rootCmd, Function<Pair<String[], CommandSender>, Boolean> runnable) {
        return registerCommands.add(new com.sen.Command(rootCmd, runnable));
    }
}
