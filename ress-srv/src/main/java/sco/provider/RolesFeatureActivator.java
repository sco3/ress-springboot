package sco.provider;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

@Provider
public class RolesFeatureActivator extends RolesAllowedDynamicFeature {

}
