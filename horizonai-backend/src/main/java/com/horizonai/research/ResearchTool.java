package com.horizonai.research;

import java.util.List;

public interface ResearchTool {

    String name();

    List<Evidence> execute(ResearchToolContext context);
}
