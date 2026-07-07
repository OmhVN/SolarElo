package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class DialogInputHelper {

    private static Component parseComponent(String text) {
        if (text == null) return Component.empty();
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(text.replace("§", "&"));
    }

    public static void showEloInputDialog(SolarElo plugin, Player admin, UUID targetUuid, String targetName, String action) {
        String actionUpper = action.toUpperCase();
        String promptTitle;
        String promptBody;
        String fieldLabel;

        if ("ADD".equals(actionUpper)) {
            promptTitle = plugin.getMessageManager().get("dialog-title-add", "Cộng ELO");
            promptBody = plugin.getMessageManager().get("dialog-body-add", "Nhập số ELO muốn CỘNG cho {player}:").replace("{player}", targetName);
            fieldLabel = plugin.getMessageManager().get("dialog-field-add", "Số ELO cần cộng");
        } else if ("SET".equals(actionUpper)) {
            promptTitle = plugin.getMessageManager().get("dialog-title-set", "Đặt ELO");
            promptBody = plugin.getMessageManager().get("dialog-body-set", "Nhập số ELO muốn ĐẶT cho {player}:").replace("{player}", targetName);
            fieldLabel = plugin.getMessageManager().get("dialog-field-set", "Số ELO cần đặt");
        } else {
            promptTitle = plugin.getMessageManager().get("dialog-title-remove", "Trừ ELO");
            promptBody = plugin.getMessageManager().get("dialog-body-remove", "Nhập số ELO muốn TRỪ của {player}:").replace("{player}", targetName);
            fieldLabel = plugin.getMessageManager().get("dialog-field-remove", "Số ELO cần trừ");
        }

        final String INPUT_KEY = "elo_amount_input";

        DialogAction submitAction = DialogAction.customClick(
            (view, audience) -> {
                if (!(audience instanceof Player pAdmin)) return;
                String rawInput = view.getText(INPUT_KEY);
                if (rawInput == null) return;
                String userInput = rawInput.trim();

                plugin.runForEntity(pAdmin, () -> {
                    int amount;
                    try {
                        amount = Integer.parseInt(userInput);
                    } catch (NumberFormatException e) {
                        String invalidMsg = plugin.getMessageManager().get("dialog-invalid-number", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cGiá trị nhập vào phải là số nguyên! &7Thử lại.");
                        pAdmin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize(invalidMsg));

                        showEloInputDialog(plugin, pAdmin, targetUuid, targetName, action);
                        return;
                    }

                    switch (action.toLowerCase()) {
                        case "set" -> {
                            plugin.getEloManager().setElo(targetUuid, targetName, amount);
                            String msg = plugin.getMessageManager().get("admin-set", "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã đặt ELO của &#ffffff{player} &fthành &#00ff3c{elo} ELO&f.")
                                    .replace("{player}", targetName).replace("{elo}", String.valueOf(amount));
                            pAdmin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize(msg));
                        }
                        case "add" -> {
                            plugin.getEloManager().addElo(targetUuid, targetName, amount);
                            String msg = plugin.getMessageManager().get("admin-add", "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã cộng &#00ff3c+{amount} ELO &fcho &#ffffff{player}")
                                    .replace("{player}", targetName).replace("{amount}", String.valueOf(amount));
                            pAdmin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize(msg));
                        }
                        case "remove" -> {
                            plugin.getEloManager().removeElo(targetUuid, targetName, amount);
                            String msg = plugin.getMessageManager().get("admin-remove", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã trừ &#ff3c3c-{amount} ELO &fcủa &#ffffff{player}")
                                    .replace("{player}", targetName).replace("{amount}", String.valueOf(amount));
                            pAdmin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize(msg));
                        }
                    }
                    EloGui.openEloAdminDetail(plugin, pAdmin, targetUuid, targetName);
                });
            },
            ClickCallback.Options.builder().build()
        );

        DialogAction cancelAction = DialogAction.customClick(
            (view, audience) -> {
                if (!(audience instanceof Player pAdmin)) return;
                plugin.runForEntity(pAdmin, () -> {
                    String cancelMsg = plugin.getMessageManager().get("dialog-cancel-message", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &7Đã hủy yêu cầu chỉnh sửa.");
                    pAdmin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize(cancelMsg));
                    EloGui.openEloAdminDetail(plugin, pAdmin, targetUuid, targetName);
                });
            },
            ClickCallback.Options.builder().build()
        );

        String btnConfirm = plugin.getMessageManager().get("dialog-button-confirm", "Xác nhận");
        String btnConfirmHover = plugin.getMessageManager().get("dialog-button-confirm-hover", "Click để xác nhận số ELO");
        String btnCancel = plugin.getMessageManager().get("dialog-button-cancel", "Hủy");
        String btnCancelHover = plugin.getMessageManager().get("dialog-button-cancel-hover", "Click để hủy và quay lại");

        Dialog dialog = Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(parseComponent(promptTitle))
                .body(List.of(
                    DialogBody.plainMessage(parseComponent(promptBody))
                ))
                .inputs(List.of(
                    DialogInput.text(INPUT_KEY, parseComponent(fieldLabel))
                        .width(250)
                        .labelVisible(true)
                        .initial("")
                        .maxLength(10)
                        .build()
                ))
                .canCloseWithEscape(true)
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.create(
                    parseComponent(btnConfirm),
                    parseComponent(btnConfirmHover),
                    100,
                    submitAction
                ),
                ActionButton.create(
                    parseComponent(btnCancel),
                    parseComponent(btnCancelHover),
                    100,
                    cancelAction
                )
            ))
        );

        admin.showDialog(dialog);
    }

    public static void showSearchDialog(SolarElo plugin, Player admin) {
        final String INPUT_KEY = "player_name_input";

        String promptTitle = plugin.getMessageManager().get("search-dialog-title", "Tìm kiếm người chơi");
        String promptBody  = plugin.getMessageManager().get("search-dialog-body",  "Nhập tên người chơi cần tìm:");
        String fieldLabel  = plugin.getMessageManager().get("search-dialog-field", "Tên người chơi");
        String btnConfirm  = plugin.getMessageManager().get("dialog-button-confirm", "Tìm kiếm");
        String btnConfirmHover = plugin.getMessageManager().get("dialog-button-confirm-hover", "Click để tìm kiếm");
        String btnCancel   = plugin.getMessageManager().get("dialog-button-cancel", "Hủy");
        String btnCancelHover = plugin.getMessageManager().get("dialog-button-cancel-hover", "Click để hủy và quay lại");

        DialogAction submitAction = DialogAction.customClick(
            (view, audience) -> {
                if (!(audience instanceof Player pAdmin)) return;
                String rawInput = view.getText(INPUT_KEY);
                if (rawInput == null) return;
                String input = rawInput.trim();

                if (input.isEmpty()) {
                    plugin.runForEntity(pAdmin, () -> showSearchDialog(plugin, pAdmin));
                    return;
                }

                plugin.runAsync(() -> FloodgateFormHelper.resolveAndOpen(plugin, pAdmin, input));
            },
            ClickCallback.Options.builder().build()
        );

        DialogAction cancelAction = DialogAction.customClick(
            (view, audience) -> {
                if (!(audience instanceof Player pAdmin)) return;
                plugin.runForEntity(pAdmin, () -> {
                    String cancelMsg = plugin.getMessageManager().get("dialog-cancel-message", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &7Đã hủy tìm kiếm.");
                    pAdmin.sendMessage(dev.solar.solarelo.managers.EloManager.colorize(cancelMsg));
                    EloGui.openEloAdmin(plugin, pAdmin);
                });
            },
            ClickCallback.Options.builder().build()
        );

        Dialog dialog = Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(parseComponent(promptTitle))
                .body(List.of(
                    DialogBody.plainMessage(parseComponent(promptBody))
                ))
                .inputs(List.of(
                    DialogInput.text(INPUT_KEY, parseComponent(fieldLabel))
                        .width(250)
                        .labelVisible(true)
                        .initial("")
                        .maxLength(32)
                        .build()
                ))
                .canCloseWithEscape(true)
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.create(parseComponent(btnConfirm), parseComponent(btnConfirmHover), 100, submitAction),
                ActionButton.create(parseComponent(btnCancel),  parseComponent(btnCancelHover),  100, cancelAction)
            ))
        );

        admin.showDialog(dialog);
    }
}
