/*******************************************************************************
Copyright (c) 2005-2009 David Williams

This software is provided 'as-is', without any express or implied
warranty. In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not
    claim that you wrote the original software. If you use this software
    in a product, an acknowledgment in the product documentation would be
    appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be
    misrepresented as being the original software.

    3. This notice may not be removed or altered from any source
    distribution.
*******************************************************************************/

#ifndef __PolyVox_AStarPathfinderImpl_H__
#define __PolyVox_AStarPathfinderImpl_H__

#include "PolyVoxCore/Vector.h"

#include <algorithm>
#include <limits> //For numeric_limits
#include <set>
#include <vector>

namespace PolyVox
{
	class OpenNodesContainer;
	class ClosedNodesContainer;
	class ThermiteGameLogic;

	/// The Connectivity of a voxel determines how many neighbours it has.
	enum Connectivity
	{
		/// Each voxel has six neighbours, which are those sharing a face.
		SixConnected,
		/// Each voxel has 18 neighbours, which are those sharing a face or an edge.
		EighteenConnected,
		/// Each voxel has 26 neighbours, which are those sharing a face, edge, or corner.
		TwentySixConnected
	};

	struct Node
	{
		Node(int x, int y, int z)
			:gVal(std::numeric_limits<float>::quiet_NaN()) //Initilise with NaNs so that we will
			,hVal(std::numeric_limits<float>::quiet_NaN()) //know if we forget to set these properly.
			,parent(0)
		{
			position.setX(x);
			position.setY(y);
			position.setZ(z);
		}

		bool operator==(const Node& rhs) const
		{
			return position == rhs.position;
		}

		bool operator<(const Node& rhs) const
		{
			if (position.getX() < rhs.position.getX())
				return true;
			if (rhs.position.getX() < position.getX())
				return false;

			if (position.getY() < rhs.position.getY())
				return true;
			if (rhs.position.getY() < position.getY())
				return false;

			if (position.getZ() < rhs.position.getZ())
				return true;
			if (rhs.position.getZ() < position.getZ())
				return false;

			return false;
		}

		PolyVox::Vector3DInt32 position;
		float gVal;
		float hVal;
		Node* parent;

		float f(void) const
		{
			float f = gVal + hVal;
			return f;
		}
	};

	typedef std::set<Node> AllNodesContainer;

	class AllNodesContainerIteratorComparator
	{
	public:
		bool operator() (const AllNodesContainer::iterator& lhs, const  AllNodesContainer::iterator& rhs) const
		{
			return (&(*lhs)) < (&(*rhs));
		}
	};

	class NodeSort
	{
	public:
		bool operator() (const AllNodesContainer::iterator& lhs, const AllNodesContainer::iterator& rhs) const
		{
			return lhs->f() > rhs->f();
		}
	};

	class OpenNodesContainer
	{
	public:
		typedef std::vector<AllNodesContainer::iterator>::iterator iterator;

	public:
		void clear(void)
		{
			open.clear();
		}

		bool empty(void) const
		{
			return open.empty();
		}

		void insert(AllNodesContainer::iterator node)
		{
			open.push_back(node);
			push_heap(open.begin(), open.end(), NodeSort());
		}

		AllNodesContainer::iterator getFirst(void)
		{
			return open[0];
		}

		void removeFirst(void)
		{
			pop_heap(open.begin(), open.end(), NodeSort());
			open.pop_back();
		}

		void remove(iterator iterToRemove)
		{
			open.erase(iterToRemove);
			make_heap(open.begin(), open.end(), NodeSort());
		}

		iterator begin(void)
		{
			return open.begin();
		}

		iterator end(void)
		{
			return open.end();
		}

		iterator find(AllNodesContainer::iterator node)
		{
			std::vector<AllNodesContainer::iterator>::iterator openIter = std::find(open.begin(), open.end(), node);
			return openIter;
		}

	private:
		std::vector<AllNodesContainer::iterator> open;
	};

	class ClosedNodesContainer
	{
	public:
		typedef std::set<AllNodesContainer::iterator, AllNodesContainerIteratorComparator>::iterator iterator;

	public:
		void clear(void)
		{
			closed.clear();
		}

		void insert(AllNodesContainer::iterator node)
		{
			closed.insert(node);
		}

		void remove(iterator iterToRemove)
		{
			closed.erase(iterToRemove);
		}

		iterator begin(void)
		{
			return closed.begin();
		}

		iterator end(void)
		{
			return closed.end();
		}

		iterator find(AllNodesContainer::iterator node)
		{
			iterator iter = std::find(closed.begin(), closed.end(), node);
			return iter;
		}

	private:
		std::set<AllNodesContainer::iterator, AllNodesContainerIteratorComparator> closed;
	};


	//bool operator<(const AllNodesContainer::iterator& lhs, const  AllNodesContainer::iterator& rhs);
}

#endif //__PolyVox_AStarPathfinderImpl_H__
