package com.lvdriver.letyoudespawn.client;

import com.lvdriver.letyoudespawn.Constants;
import com.lvdriver.letyoudespawn.LetYouDespawn;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * One searchable registry browser for both lists: the tab bar switches between entity types and
 * items. Clicking a row adds the id to the matching list, or removes it when already present.
 */
public class RegistryPickerScreen extends Screen {
   private final Screen parent;
   private final List<Row> mobRows = new ArrayList<>();
   private final List<Row> itemRows = new ArrayList<>();
   private final List<Row> visibleRows = new ArrayList<>();
   private boolean mobs = true;
   private EditBox search;
   private EntryList list;
   private Button mobTab;
   private Button itemTab;

   public RegistryPickerScreen(Screen parent) {
      super(Component.translatable("letyoudespawn.picker.title"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      if (this.mobRows.isEmpty()) {
         this.buildRows();
      }

      this.mobTab = this.addRenderableWidget(Button.builder(Component.translatable("letyoudespawn.picker.tab.mobs"),
            button -> this.switchTo(true))
         .bounds(this.width / 2 - 102, 24, 100, 20)
         .build());
      this.itemTab = this.addRenderableWidget(Button.builder(Component.translatable("letyoudespawn.picker.tab.items"),
            button -> this.switchTo(false))
         .bounds(this.width / 2 + 2, 24, 100, 20)
         .build());

      this.search = new EditBox(this.font, this.width / 2 - 150, 50, 300, 18,
         Component.translatable("letyoudespawn.picker.search"));
      this.search.setResponder(this::applyFilter);
      this.search.setHint(Component.translatable("letyoudespawn.picker.search").withStyle(ChatFormatting.DARK_GRAY));
      this.addRenderableWidget(this.search);

      this.list = new EntryList(this.minecraft, this.width, this.height - 76 - 40, 76, 20);
      this.addRenderableWidget(this.list);

      this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
         .bounds(this.width / 2 - 75, this.height - 32, 150, 20)
         .build());

      this.switchTo(this.mobs);
   }

   private void buildRows() {
      BuiltInRegistries.ENTITY_TYPE.keySet().forEach(id ->
         this.mobRows.add(new Row(id, BuiltInRegistries.ENTITY_TYPE.get(id).getDescription().getString())));
      BuiltInRegistries.ITEM.keySet().forEach(id ->
         this.itemRows.add(new Row(id, BuiltInRegistries.ITEM.get(id).getDescription().getString())));
      this.mobRows.sort(Comparator.comparing(row -> row.id.toString()));
      this.itemRows.sort(Comparator.comparing(row -> row.id.toString()));
   }

   private void switchTo(boolean mobMode) {
      this.mobs = mobMode;
      if (this.mobTab != null) {
         this.mobTab.active = !mobMode;
         this.itemTab.active = mobMode;
      }
      if (this.search != null) {
         this.search.setValue("");
      }
      this.visibleRows.clear();
      this.visibleRows.addAll(mobMode ? this.mobRows : this.itemRows);
      this.rebuildList();
   }

   private void applyFilter(String raw) {
      String query = raw.toLowerCase().trim();
      List<Row> source = this.mobs ? this.mobRows : this.itemRows;
      this.visibleRows.clear();
      if (query.isEmpty()) {
         this.visibleRows.addAll(source);
      } else {
         for (Row row : source) {
            if (row.id.toString().contains(query) || row.name.toLowerCase().contains(query)) {
               this.visibleRows.add(row);
            }
         }
      }
      this.rebuildList();
   }

   private void rebuildList() {
      if (this.list != null) {
         this.list.refresh(this.visibleRows);
      }
   }

   private boolean isSelected(ResourceLocation id) {
      return this.mobs
         ? Constants.config.getMobNames().contains(id.toString())
         : Constants.config.getPersistenceEnablers().contains(id.toString());
   }

   private void toggle(ResourceLocation id) {
      if (this.mobs) {
         if (this.isSelected(id)) {
            Constants.config.removeMobName(id.toString());
         } else {
            Constants.config.addMobName(id.toString());
         }
      } else if (this.isSelected(id)) {
         Constants.config.removePersistenceEnabler(id.toString());
      } else {
         Constants.config.addPersistenceEnabler(id.toString());
      }
      LetYouDespawn.syncToFzzy();
   }

   @Override
   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 9, 0xFFFFFF);
   }

   @Override
   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   private final class EntryList extends ObjectSelectionList<EntryList.Entry> {
      private EntryList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
         super(minecraft, width, height, y, itemHeight);
      }

      private void refresh(List<Row> rows) {
         this.clearEntries();
         for (Row row : rows) {
            this.addEntry(new Entry(row));
         }
      }

      private final class Entry extends ObjectSelectionList.Entry<Entry> {
         private final Row row;

         private Entry(Row row) {
            this.row = row;
         }

         @Override
         public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                            int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean selected = RegistryPickerScreen.this.isSelected(this.row.id);
            String mark = selected ? "\u2714 " : "   ";
            int color = selected ? 0x55FF55 : (hovered ? 0xFFFF55 : 0xFFFFFF);
            guiGraphics.drawString(RegistryPickerScreen.this.font, mark + this.row.name, left + 4, top + 6, color, false);
            String idText = this.row.id.toString();
            guiGraphics.drawString(RegistryPickerScreen.this.font, idText,
               left + width - RegistryPickerScreen.this.font.width(idText) - 6, top + 6,
               selected ? 0x55FF55 : 0x808080, false);
         }

         @Override
         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            RegistryPickerScreen.this.toggle(this.row.id);
            return true;
         }

         @Override
         public Component getNarration() {
            return Component.literal(this.row.name + " " + this.row.id);
         }
      }
   }

   private record Row(ResourceLocation id, String name) {
   }
}
