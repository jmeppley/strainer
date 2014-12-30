package jme.tools.misc;

import java.util.HashMap;
import java.util.Set;

public class CmdLineOptions {
	private String mProgramName = "PROGRAM";
	private String mArgUsage = "ARGUMENTS";
	private String mUsageString = null;
	private HashMap<String,Option> mOptionNameHash = null;
	private HashMap<Character,Option> mOptionCharHash = null;

	/**
	 * Define the usage
	 * @param pProgramName the name of the program (used to build usage string)
	 * @param pOptions a Set of option definitions (CmdLineOptions.Option objects)
	 * @param pArgUsage usage string for non-option arguments
	 */
	public CmdLineOptions(String pProgramName, Set<Option> pOptions, String pArgUsage) {
		mProgramName = pProgramName;
		mArgUsage = pArgUsage;
		setUsage(pOptions);
	}

	/**
	 * Define the usage
	 * @param pProgramName the name of the program (used to build usage string)
	 * @param pOptions a Set of option definitions (CmdLineOptions.Option objects)
	 */
	public CmdLineOptions(String pProgramName, Set<Option> pOptions) {
		mProgramName = pProgramName;
		setUsage(pOptions);
	}

	/**
	 * Define the usage
	 * @param pOptions a Set of option definitions (CmdLineOptions.Option objects)
	 * @param pArgUsage usage string for non-option arguments
	 */
	public CmdLineOptions(Set<Option> pOptions, String pArgUsage) {
		mArgUsage = pArgUsage;
		setUsage(pOptions);
	}
	
	/**
	 * Define the usage
	 * @param pOptions a Set of option definitions (CmdLineOptions.Option objects)
	 */
	public CmdLineOptions(Set<Option> pOptions) {
		setUsage(pOptions);
	}
	
	/**
	 * Define the usage
	 * @param pOptions a Set of option definitions (CmdLineOptions.Option objects)
	 */
	private void setUsage(Set<Option> pOptions) {
		mOptionNameHash = new HashMap<String,Option>();
		mOptionCharHash = new HashMap<Character,Option>();

		// set usage string
		StringBuffer mus = new StringBuffer();
		mus.append(mProgramName).append(" OPTIONS ").append(mArgUsage).append("\n\n");

		// loop over options, 
		for (Option opt: pOptions) {
			mus.append("  ").append(opt.getUsageString()).append("\n");
			mOptionNameHash.put(opt.mLongName,opt);
			if (opt.getChar()!=' ') {
				mOptionCharHash.put(opt.getChar(),opt);
			}
		}

		mus.append("\n");
		
		mUsageString = mus.toString();
	}

	/**
	 * Parse the command line arguments
	 * @param pArgs the command line arguments
	 * @return remaining arguments (everythin that;s not an option)
	 * @throws CmdLineParsingException if there is an error parsing 
	 * (the Exception argument can return the generated Usage string if desired) 
	 */
	public String [] parse(String [] pArgs) throws CmdLineParsingException {
		// go through args and update option objects
		int i;
		for (i = 0; i<pArgs.length; i++) {
			String arg = pArgs[i];
			if (arg.charAt(0)=='-') {
				// it's an option
				
				// what exactly is this option
				if (arg.length()==1) {
					// blank
					throw new CmdLineParsingException("Single dash (-) does not make sense without option character.");
				}

				Option opt = null;
				
				// get option object
				if (arg.charAt(1)=='-') {
					// it' a long name option

					if (arg.length()==1) {
						// blank
						throw new CmdLineParsingException("Double dash (--) does not make sense without option name.");
					}

					// update the Option object
					String optionName = arg.substring(2);
					opt = mOptionNameHash.get(optionName);
				} else {
					// it's a short name option
					char optChar = arg.charAt(1);
					opt = mOptionCharHash.get(optChar);
				}

				if (opt==null) {
					throw new CmdLineParsingException("Unknown option: '" + arg + "'");
				}

				opt.setInArgs(true);
				if (opt.requiresValue()) {
					try {
						i++;
						opt.setValue(pArgs[i]);
					} catch (IndexOutOfBoundsException e) {
						throw new CmdLineParsingException("Missing value for option: " + opt.getLongName());
					}
				}
			} else {
				// not an option (doesn't start with -)
				//  everything else is args, bail
				break;
			}
		}
		
		// args from i on up are args
		String [] newArgs = new String[pArgs.length - i];
		for (int j = 0;j<newArgs.length;j++) {
			newArgs[j] = pArgs[j+i];
		}
		return newArgs;
	}
	
	public String getUsageString() {
		return mUsageString;
	}

	/**
	 * Defines a possible command line option
	 */
	public static class Option {
		// about this option
		private String mLongName;
		private char mChar;
		private boolean mNeedsValue;
		private String mDescription;
		private String mUsageString;
		
		// state of the option
		private boolean mInArgs = false;
		private String mValue = null;
		
		/**
		 * @param pLongName the name of the option (will follow -- on the command line)
		 * @param pChar (the abbrev for us after single dash) (space (' ') means not available)
		 * @param pNeedsValue true if option must be followed by a value
		 * @param pDescription description of option
		 */
		public Option(String pLongName, char pChar, boolean pNeedsValue, String pDescription) {
			mLongName = pLongName;
			mChar = pChar;
//			mRequired = pRequired;
			mNeedsValue = pNeedsValue;
			mDescription = pDescription;
			
			setUsageString();
		}
		
		public String getLongName() {return mLongName;}
		public char getChar() {return mChar;}
//		public boolean isRequired() {return mRequired;}
		public boolean requiresValue() {return mNeedsValue;}

		public String getUsageString() { return mUsageString; }
		private void setUsageString() {
			StringBuffer us = new StringBuffer();
			us.append("--").append(mLongName);
			if (mNeedsValue) {
				us.append(" ").append("VALUE");
			}
			if (mChar!=' ') {
				us.append(" (-").append(mChar).append(")");
			}
			
			// line up next column
			for (int i = us.length(); i < 30; i++) {
				us.append(' ');
			}
			us.append(' ');
			us.append(mDescription);
			
			mUsageString = us.toString();
		}

		public void setInArgs(boolean pInArgs) { mInArgs = pInArgs; }
		public boolean isInArgs() { return mInArgs; }
		
		public void setValue(String pValue) { mValue = pValue; }
		public String getValue() { return mValue; }
	}
	
	public class CmdLineParsingException extends Exception {
		public CmdLineParsingException(String pMsg) {
			super(pMsg);
		}
		
		public String getUsage() {
			return mUsageString;
		}
	}
}
