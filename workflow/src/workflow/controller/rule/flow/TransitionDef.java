package workflow.controller.rule.flow;

import java.util.List;

import ariba.util.core.ListUtil;

public class TransitionDef {
	String sourceId;
	List<PathDef> paths;
	
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public List<PathDef> getPaths() {
		if(paths == null)
			paths = ListUtil.list();
		return paths;
	}
	public void addPath(PathDef path) {
		this.getPaths().add(path);
	}
	
	public PathDef getPath(Object model) {
		for(PathDef path : this.getPaths()) {
			if(path.evaluate(model))
				return path;
		}
		return null;
	}
	
	public List<PathDef> getPathsForModel(Object model) {
		List<PathDef> defs = ListUtil.list();
		for(PathDef path : this.getPaths()) {
			if(path.evaluate(model))
				defs.add(path);
		}
		return defs;
	}
	
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append("TransitionDef{" );
		string.append("sourceId=" + sourceId + ";");
		string.append("paths=(");
		List<PathDef> pathList = getPaths();
		for(int i = 0; i < pathList.size(); i++) {
			PathDef path = pathList.get(i);
			if(i > 0)
				string.append(", ");
			string.append(path.toString());
		}
		string.append(")}");
		return string.toString();
	}
}
