package net.prizowo.vectorientation.main.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.network.chat.Component;


public abstract class InteractiveScrollableWidget extends AbstractScrollWidget {

    private java.util.function.Consumer<Double> changedListener;

    public InteractiveScrollableWidget(int i, int j, int k, int l, Component text) {
        super(i, j, k, l, text);
    }

    public void setChangedListener(java.util.function.Consumer<Double> changedListener){
        this.changedListener = changedListener;
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        if (changedListener != null) {
            changedListener.accept(scrollAmount);
        }
    }

    public double getScrollY(){
        return this.scrollAmount();
    }

    // Use the parent's renderWidget implementation which handles everything correctly


    @Override
    protected abstract int getInnerHeight();
    @Override
    protected abstract double scrollRate();

    protected abstract void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta);
    @Override
    protected abstract void updateWidgetNarration(NarrationElementOutput narrationElementOutput);
}
