package org.to2mbn.lolixl.utils.event;

import org.osgi.service.event.Event;

import java.util.Collections;

public class ApplicationExitEvent extends Event {

	public static final String TOPIC_APPLICATION_EXIT = "org/to2mbn/lolixl/utils/event/applicationExit";

	public ApplicationExitEvent() {
		super(TOPIC_APPLICATION_EXIT, Collections.emptyMap());
	}
}
