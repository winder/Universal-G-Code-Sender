package com.willwinder.ugs.nbp.designer.gui.clipart.sources;

import com.willwinder.ugs.nbp.designer.gui.clipart.Category;
import com.willwinder.ugs.nbp.designer.gui.clipart.Clipart;
import com.willwinder.ugs.nbp.designer.gui.clipart.ClipartSource;

import java.util.List;

public abstract class AbstractClipartSource implements ClipartSource {
    public abstract List<? extends Clipart> getCliparts();

    @Override
    public List<? extends Clipart> getCliparts(Category category) {
        return getCliparts()
                .stream()
                .filter(clipart -> clipart.getCategory() == category || category == Category.ALL)
                .toList();
    }
}
