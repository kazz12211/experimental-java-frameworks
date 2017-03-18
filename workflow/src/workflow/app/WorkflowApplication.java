package workflow.app;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import workflow.aribaweb.formatter.DecimalDoubleFormatter;
import workflow.aribaweb.formatter.DecimalDoubleRUFormatter;
import workflow.aribaweb.formatter.DecimalIntFormatter;
import workflow.aribaweb.formatter.DecimalIntRUFormatter;
import workflow.aribaweb.formatter.DurationFormatter;
import workflow.aribaweb.formatter.DurationNoMillisFormatter;
import workflow.aribaweb.formatter.IntegerOrNullFormatter;
import workflow.aribaweb.formatter.NumberOrNullFormatter;
import workflow.aribaweb.formatter.PercentageFormatter;
import workflow.aribaweb.formatter.TimeFormatter;
import workflow.integration.IntegrationBus;
import workflow.util.Logging;
import ariba.ui.aribaweb.core.AWComponentActionRequestHandler;
import ariba.ui.aribaweb.core.AWConcreteApplication;
import ariba.ui.aribaweb.core.AWLocalLoginSessionHandler;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.aribaweb.core.AWSessionValidationException;
import ariba.ui.aribaweb.core.AWLocalLoginSessionHandler.CompletionCallback;
import ariba.ui.aribaweb.util.AWFormatter;
import ariba.ui.aribaweb.util.AWMultiLocaleResourceManager;
import ariba.ui.servletadaptor.AWServletApplication;
import ariba.ui.validation.AWVFormatterFactory;
import ariba.ui.validation.AWVFormatterFactory.FormatterProvider;
import ariba.ui.widgets.ActionHandler;
import ariba.ui.widgets.AribaAction;
import ariba.ui.widgets.ConditionHandler;
import ariba.util.core.MapUtil;

public abstract class WorkflowApplication extends AWServletApplication {

	private IntegrationBus integrationBus;
	private WorkflowUserBinder userBinder;
	
	@Override
	public void init() {
		super.init();
		
		userBinder = createUserBinder();
		userBinder.init();
		
		this.setupStringHandlers();
		this.setupSessionValidator(true);
		this.installFormatters();

		String resourceUrl = this.resourceUrl();
		AWMultiLocaleResourceManager resourceManager = this.resourceManager();
		resourceManager.registerResourceDirectory("./rule", resourceUrl + "rule", false);
		resourceManager.registerResourceDirectory("./rule-backup", resourceUrl + "rule-backup", false);
		resourceManager.registerResourceDirectory("./mail-template", resourceUrl + "mail-template", false);
		resourceManager.registerResourceDirectory("./rdbms", resourceUrl + "rdbms", false);
		resourceManager.registerResourceDirectory("./universe", resourceUrl + "universe", false);
		resourceManager.registerResourceDirectory("./jdbc", resourceUrl + "jdbc", false);
		resourceManager.registerResourceDirectory("./letter", resourceUrl + "letter", false);
		
		workflow.model.Initialization.initialize();
		
		integrationBus = new IntegrationBus(5);
		integrationBus.start();
		
		this.setSessionTimeout(3600);
	}
	
	protected abstract void setupStringHandlers();
	protected abstract WorkflowLoginPage getLoginPage(AWRequestContext requestContext);

	protected void setupSessionValidator(final boolean allowAccessWithoutLogin) {
        AWConcreteApplication application = (AWConcreteApplication)this;
        application.setSessionValidator(new AWLocalLoginSessionHandler() {
            protected AWResponseGenerating showLoginPage (AWRequestContext requestContext,
                                                          CompletionCallback callback)
            {
            	WorkflowLoginPage loginPage = getLoginPage(requestContext);
                loginPage.init(callback);
                return loginPage;
            }

            protected boolean requireSessionValidationForAllComponentActions ()
            {
                return !allowAccessWithoutLogin;
            }

            protected boolean validateSession (AWRequestContext requestContext)
            {
                return getUserBinder().isLoggedIn();
            }

			@Override
			public AWResponseGenerating handleSessionRestorationError(
					AWRequestContext requestContext) {
				Logging.custom.debug("handleSessionRestorationError()");
				return requestContext.application().mainPage(requestContext);
			}


        });

        ActionHandler.setHandler(AribaAction.LogoutAction, new ActionHandler() {
            public AWResponseGenerating actionClicked (AWRequestContext requestContext)
            {
                // Force user to anonymous and kill session
                //UserBinder.bindUserToSession(User.getAnonymous(), requestContext.session());
                // MetaNavTabBar.invalidateState(requestContext.session());
                requestContext.session().terminate();
                return AWComponentActionRequestHandler.SharedInstance.processFrontDoorRequest(requestContext);
            }
        });

        ConditionHandler.setHandler("disableLogoutAction", new ConditionHandler() {
            public boolean evaluateCondition (AWRequestContext requestContext)
            {
                return !getUserBinder().isLoggedIn();
            }
        });

        if (allowAccessWithoutLogin) {
            ConditionHandler.setHandler("showLoginAction", new ConditionHandler() {
                public boolean evaluateCondition (AWRequestContext requestContext)
                {
                    return !getUserBinder().isLoggedIn();
                }
            });

            ActionHandler.setHandler("login", new ActionHandler() {
                public AWResponseGenerating actionClicked (AWRequestContext requestContext)
                {
                    // force a login
                    if (!getUserBinder().isLoggedIn()) throw new AWSessionValidationException();
                    return null;
                }

                public boolean submitFormToComponentAction ()
                {
                    // we're going to change the structure of the page, so we don't want form vals for the replay
                    return false;
                }
            });
        }
	}

	protected Map<String, AWFormatter> createExtraFormatters() {
		Map<String, AWFormatter> formatters = MapUtil.map();
		formatters.put("decimalInt", new DecimalIntFormatter());
		formatters.put("decimalIntRU", new DecimalIntRUFormatter());
		formatters.put("decimalDouble", new DecimalDoubleFormatter());
		formatters.put("decimalDoubleRU", new DecimalDoubleRUFormatter());
		formatters.put("percentage", new PercentageFormatter());
		formatters.put("numberOrNull", new NumberOrNullFormatter());
		formatters.put("integerOrNull", new IntegerOrNullFormatter());
		formatters.put("duration", new DurationFormatter());
		formatters.put("hms", new DurationNoMillisFormatter());
		formatters.put("time", new TimeFormatter());
		return formatters;
	}
	
	private void installFormatters() {
		AWVFormatterFactory.registerProvider(new FormatterProvider() {

			@Override
			public void populateFormatters(Map<String, Object> formatters,
					Locale locale, TimeZone timeZone) {
				Map<String, AWFormatter> map = createExtraFormatters();
				for(Map.Entry<String, AWFormatter> entry : map.entrySet()) {
					formatters.put(entry.getKey(), entry.getValue());
				}
			}});
	}

	@Override
	public void terminate() {
		integrationBus.stop();
		super.terminate();
	}


	public IntegrationBus getIntegrationBus() {
		return integrationBus;
	}
	
	protected abstract WorkflowUserBinder createUserBinder();
	
	public WorkflowUserBinder getUserBinder() {
		return userBinder;
	}
}
