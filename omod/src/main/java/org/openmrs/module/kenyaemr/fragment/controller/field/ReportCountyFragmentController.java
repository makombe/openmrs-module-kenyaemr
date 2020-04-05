/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.fragment.controller.field;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaui.KenyaUiUtils;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller for report period field fragments
 */
public class ReportCountyFragmentController {

	public void controller(
						   FragmentModel model,
						   @SpringBean KenyaUiUtils kenyaui) {

		// create list of counties

		List<String> countyList = new ArrayList<String>();
		List<Location> locationList = Context.getLocationService().getAllLocations();
		for(Location loc: locationList) {
			String locationCounty = loc.getCountyDistrict();
			if(!StringUtils.isEmpty(locationCounty) && !StringUtils.isBlank(locationCounty)) {
				countyList.add(locationCounty);

			}
		}

		Set<String> uniqueCountyList = new HashSet<String>(countyList);
		model.addAttribute("countyList", uniqueCountyList);


	}
}