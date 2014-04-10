package uk.ac.starlink.vo;

import java.io.IOException;
import java.net.URL;

/**
 * Defines the details of a registry access protocol.
 *
 * @author   Mark Taylor
 * @since    9 Apr 2014
 */
public abstract class RegistryProtocol {

    private final String shortName_;
    private final String fullName_;
    private final String[] dfltUrls_;

    /** Protocol instance for Registry Interface 1.0. */
    public static final RegistryProtocol RI1 = new Ri1RegistryProtocol();

    /** Known protocols. */
    public static final RegistryProtocol[] PROTOCOLS = { RI1 };

    /**
     * Constructor.
     *
     * @param  shortName  short name
     * @param  fullName   full name
     * @param  dfltUrls  strings giving some default registry endpoints for 
     *                   this access protocol
     */
    protected RegistryProtocol( String shortName, String fullName,
                                String[] dfltUrls ) {
        shortName_ = shortName;
        fullName_ = fullName;
        dfltUrls_ = dfltUrls.clone();
    }

    /**
     * Returns a short name for this protocol.
     *
     * @return   short name
     */
    public String getShortName() {
        return shortName_;
    }

    /**
     * Returns the full name for this protocol.
     *
     * @return  full name
     */
    public String getFullName() {
        return fullName_;
    }

    /**
     * Returns default endpoint URLs for this protocol.
     *
     * @return  endpoint URL strings
     */
    public String[] getDefaultRegistryUrls() {
        return dfltUrls_.clone();
    }

    /**
     * Searches a given registry to discover new endpoint URLs serving
     * this registry protocol.
     *
     * @param  regUrl0  bootstrap registry endpoint URL
     * @return   registry endpoint URLs discovered from the registry
     */
    public abstract String[] discoverRegistryUrls( String regUrl0 )
            throws IOException;

    /**
     * Constructs a registry query that gets results for a list of
     * given IVO ID strings, optionally restricted by a given capability.
     * The resulting query supplies results for each resource which is
     * all of: 
     * (a) in the registry,
     * (b) in the <code>ivoids</code> list, and
     * (c) has the given capability
     * If <code>capability</code> is null, then restriction (c) does not apply.
     *
     * @param  ivoids  ID values for the required resources
     * @param  capability  service capability type, or null
     * @param  regUrl   endpoint URL for a registry service implementing
     *                  this protocol
     * @return  registry query
     */
    public abstract RegistryQuery createIdListQuery( String[] ivoids,
                                                     Capability capability,
                                                     URL regUrl );

    /**
     * Constructs a registry query that gets results for resources with
     * a match for one or all of a given set of keywords found in
     * a selection of resource fields.
     * 
     * @param  keywords  single-word keywords to match independently
     * @param  rfs   resource fields against which keywords are to match
     * @param  isOr  if false all keywords must match,
     *               if true at least one keyword must match
     * @param  capability   if non-null, restricts the resources to those
     *                      with that capability
     * @param  regUrl   endpoint URL for a registry service implementing
     *                  this protocol
     * @return  registry query
     */
    public abstract RegistryQuery createKeywordQuery( String[] keywords,
                                                      ResourceField[] rfs,
                                                      boolean isOr,
                                                      Capability capability,
                                                      URL regUrl );

    /**
     * Indicates whether a given RegCapabilityInterface object is an
     * instance of this capability.
     *
     * <p>Really, the implementation of this ought not to be a function
     * of the registry protocol in use.  However, it's probably the case
     * that the different registry implementations have different quirks
     * in this respect, so take the opportunity to parameterise it by
     * registry protocol in case that's required.
     *
     * @param  stdCap  standard capability definition
     * @param  resCap  capability interface object representing part of
     *                 a registry resource
     * @return  true iff <code>resCap</code> represents a capability
     *          of the type <code>stdCap</code>
     */
    public abstract boolean hasCapability( Capability stdCap,
                                           RegCapabilityInterface resCap );

    /**
     * RegistryProtocol implementation for Registry Interface 1.0.
     */
    private static class Ri1RegistryProtocol extends RegistryProtocol {

        /**
         * Constructor.
         */
        Ri1RegistryProtocol() {
            super( "RI1.0", "Registry Interface 1.0",
                   Ri1RegistryQuery.REGISTRIES );
        }

        public String[] discoverRegistryUrls( String regUrl0 )
                throws IOException {
            return Ri1RegistryQuery.getSearchableRegistries( regUrl0 );
        }

        public RegistryQuery createIdListQuery( String[] ivoids,
                                                Capability capability,
                                                URL regUrl ) {
            if ( ivoids == null || ivoids.length == 0 ) {
                return null;
            }
            StringBuffer sbuf = new StringBuffer();
            if ( capability != null ) {
                sbuf.append( "(" )
                    .append( Ri1RegistryQuery.getAdqlWhere( capability ) )
                    .append( ")" );
            }
            if ( sbuf.length() > 0 ) {
                sbuf.append( " AND " );
            }
            sbuf.append( "(" );
            for ( int i = 0; i < ivoids.length; i++ ) {
                if ( i > 0 ) {
                    sbuf.append( " OR " );
                }
                sbuf.append( "identifier = '" )
                    .append( ivoids[ i ] )
                    .append( "'" );
            }
            sbuf.append( ")" );
            String adql = sbuf.toString();
            return new Ri1RegistryQuery( regUrl.toString(), adql );
        }

        public RegistryQuery createKeywordQuery( String[] keywords,
                                                 ResourceField[] fields,
                                                 boolean isOr,
                                                 Capability capability,
                                                 URL regUrl ) {
            String conjunction = isOr ? " OR " : " AND ";
            StringBuffer sbuf = new StringBuffer();
            if ( capability != null ) {
                sbuf.append( Ri1RegistryQuery.getAdqlWhere( capability ) );
            }
            if ( keywords.length > 0 ) {
                if ( sbuf.length() > 0 ) {
                    sbuf.append( " AND ( " );
                }
                for ( int iw = 0; iw < keywords.length; iw++ ) {
                    if ( iw > 0 ) {
                        sbuf.append( conjunction );
                    }
                    sbuf.append( "(" );
                    for ( int ip = 0; ip < fields.length; ip++ ) {
                        if ( ip > 0 ) {
                            sbuf.append( " OR " );
                        }
                        sbuf.append( fields[ ip ].getXpath() )
                            .append( " LIKE " )
                            .append( "'%" )
                            .append( keywords[ iw ] )
                            .append( "%'" );
                    }
                    sbuf.append( ")" );
                }
                sbuf.append( " )" );
            }
            String adql = sbuf.toString();
            return new Ri1RegistryQuery( regUrl.toString(), adql );
        }

        public boolean hasCapability( Capability stdCap,
                                      RegCapabilityInterface resCap ) {
            String resType = resCap.getXsiType();
            String stdTypeTail = stdCap.getXsiTypeTail();
            String resId = resCap.getStandardId();
            String stdId = stdCap.getStandardId();
            return stdId.equals( resId )
                || ( resType != null && stdTypeTail != null
                                     && resType.endsWith( stdTypeTail ) );
        }
    };
}
