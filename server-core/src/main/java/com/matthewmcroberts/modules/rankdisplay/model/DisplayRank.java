package com.matthewmcroberts.modules.rankdisplay.model;

import com.matthewmcroberts.utils.provider.DisplayNameProvider;
import com.matthewmcroberts.utils.provider.IdProvider;

public interface DisplayRank extends IdProvider, DisplayNameProvider {
    int getPriority();
}
